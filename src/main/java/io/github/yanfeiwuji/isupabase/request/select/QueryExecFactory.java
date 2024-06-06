package io.github.yanfeiwuji.isupabase.request.select;

import java.util.*;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.relation.ToManyRelation;
import com.mybatisflex.core.table.TableInfo;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import io.github.yanfeiwuji.isupabase.request.token.KeyValue;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import io.github.yanfeiwuji.isupabase.request.utils.ValueUtils;
import lombok.experimental.UtilityClass;
import org.springframework.util.MultiValueMap;

@UtilityClass
public class QueryExecFactory {

    public QueryExecLookup of(MultiValueMap<String, String> params, TableInfo tableInfo) {

        String selectValue = Optional
                .ofNullable(params.getFirst(CommonStr.SELECT))
                .orElse(CommonStr.STAR);
        Map<String, QueryExec> lookup = new HashMap<>();

        QueryExec queryExec = QueryExecFactory.of(new QueryExecStuff(selectValue, tableInfo), lookup,
                CharSequenceUtil.EMPTY);

        return new QueryExecLookup(queryExec, lookup);
    }

    // one time to get indexed and
    private QueryExec of(QueryExecStuff stuff, Map<String, QueryExec> indexed, String pre) {

        QueryExec queryExec = new QueryExec();
        TableInfo tableInfo = stuff.tableInfo();
        queryExec.setTableInfo(tableInfo);
        queryExec.setQueryTable(CacheTableInfoUtils.nNQueryTable(tableInfo));
        queryExec.setRelation(stuff.relation());
        queryExec.setInner(stuff.inner());

        Optional<String> relEndOpt = Optional.ofNullable(queryExec.getRelation())
                .map(rel -> CharSequenceUtil.split(rel.getName(), StrPool.DOT).getLast())
                .map(CacheTableInfoUtils::propertyToParamKey);
        relEndOpt.ifPresent(queryExec::setRelEnd);

        String needPre = relEndOpt.map(next -> CharSequenceUtil.isEmpty(pre) ? next : pre + StrPool.DOT + next)
                .map(CacheTableInfoUtils::propertyToParamKey)
                .orElse(CharSequenceUtil.EMPTY);

        if (Objects.nonNull(queryExec.getRelation())) {
            indexed.put(needPre, queryExec);
        }

        final List<String> selects = TokenUtils.splitByComma(stuff.select());
        if (Objects.nonNull(queryExec.getRelation()) && selects.isEmpty()) {
            queryExec.setNotExec(true);
            return queryExec;
        }

        selects.parallelStream().forEach(selectItem -> MTokens.SELECT_WITH_SUB.keyValue(selectItem)
                .ifPresentOrElse(
                        it -> selectSub(queryExec, tableInfo, selectItem, it, indexed, needPre),
                        () -> selectColumn(queryExec, tableInfo, selectItem)));

        return queryExec;
    }

    public void assembly(QueryExecLookup queryExecLookup, MultiValueMap<String, String> params) {
        QueryExec rootQueryExec = queryExecLookup.queryExec();
        Map<String, QueryExec> indexed = queryExecLookup.indexed();
        params.forEach((k, values) -> MTokens.WITH_SUB_KEY.keyValue(k).ifPresentOrElse(kv -> {
            QueryExec queryExec = Optional.ofNullable(indexed.get(kv.key()))
                    .orElseThrow(PgrstExFactory.exEmbeddedApplyButNotInSelect(kv.key()));
            assemblySingle(queryExec, kv.value(), values);
        }, () -> assemblySingle(rootQueryExec, k, values)));
    }

    private void assemblySingle(QueryExec queryExec, String key, List<String> values) {
        QueryExecAssembleManager.assembleLimitOffsetOrder(key)
                .ifPresentOrElse(it -> it.accept(queryExec, values),
                        () -> QueryExecAssembleManager.assembleFilter(queryExec, key, values));
    }

    private QueryExecStuff queryExecStuff(KeyValue keyValue, TableInfo tableInfo) {
        boolean inner = keyValue.key().endsWith(CommonStr.SELECT_INNER_MARK);
        String key = CharSequenceUtil.replace(keyValue.key(), CommonStr.SELECT_INNER_MARK, CharSequenceUtil.EMPTY);
        AbstractRelation<?> relation = CacheTableInfoUtils.nNRealRelation(key, tableInfo);
        TableInfo innerTableInfo = TableInfoFactory.ofEntityClass(relation.getTargetEntityClass());
        return new QueryExecStuff(keyValue.value(), innerTableInfo, inner, relation);
    }

    private void addCast(QueryExec queryExec, String key, String cast) {
        ValueUtils.checkCastKey(cast);
        queryExec.addCastKey(key, cast);
    }

    private void selectColumn(QueryExec queryExec, TableInfo tableInfo, String selectItem) {
        if (CommonStr.STAR.equals(selectItem)) {
            queryExec.addQueryColumn(CacheTableInfoUtils.nNQueryAllColumns(tableInfo));
        } else {
            final String item = MTokens.SELECT_ITEM.first(selectItem).orElse(CharSequenceUtil.EMPTY);
            final QueryColumn queryColumn = CacheTableInfoUtils.nNRealQueryColumn(item, tableInfo);
            queryExec.addQueryColumn(queryColumn);
            MTokens.RENAME.first(selectItem)
                    .ifPresent(rename -> queryExec.addRename(item, rename));
            MTokens.CAST.first(selectItem)
                    .ifPresent(cast -> QueryExecFactory.addCast(queryExec, item, cast));
        }
    }

    private void selectSub(QueryExec queryExec, TableInfo tableInfo, String selectItem, KeyValue keyValue,
            Map<String, QueryExec> indexed, String needPre) {
        QueryExec subQueryExec = QueryExecFactory.of(
                QueryExecFactory.queryExecStuff(keyValue, tableInfo),
                indexed, needPre);

        queryExec.addSub(subQueryExec);

        if (subQueryExec.isNotExec()) {
            queryExec.removePickKey(subQueryExec.getRelEnd());
        }

        Optional.ofNullable(subQueryExec.getRelation())
                .ifPresent(rel -> queryExec.putSubRelMap(subQueryExec.getRelEnd(), rel));

        handlerSubSpread(selectItem, queryExec, subQueryExec);
        MTokens.RENAME.first(selectItem).ifPresent(rename -> queryExec.addRename(keyValue.key(), rename));
    }

    private void handlerSubSpread(String selectItem, QueryExec rootQueryExec, QueryExec subQueryExec) {
        final boolean spread = selectItem.startsWith(CommonStr.SPREAD_MARK);
        if (spread) {
            final AbstractRelation<?> relation = subQueryExec.getRelation();
            if (relation instanceof ToManyRelation<?>) {
                throw PgrstExFactory.exCanNotSpreadRelForManyEnd(
                        rootQueryExec.getQueryTable().getName(),
                        subQueryExec.getQueryTable().getName()).get();
            }
            Optional.ofNullable(subQueryExec.getPickKeyMap()).map(Map::keySet)
                    .ifPresent(rootQueryExec::addPickKeys);
            rootQueryExec.removePickKey(subQueryExec.getRelEnd());
        }
        subQueryExec.setSpread(spread);
    }

}
