package io.github.yanfeiwuji.isupabase.request.select;

import java.util.*;
import java.util.stream.Collectors;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryTable;
import com.mybatisflex.core.table.TableInfo;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.SelectUtils;
import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

@UtilityClass
@Slf4j
public class QueryExecFactory {

    public QueryExecLookup of(MultiValueMap<String, String> params, TableInfo tableInfo) {

        String selectValue = Optional
                .ofNullable(params.getFirst(CommonStr.SELECT))
                .orElse(CommonStr.STAR);

        Map<String, QueryExec> lookup = new HashMap<>();
        long start = System.currentTimeMillis();

        log.info("start: time:{}", start);
        List<String> removeJsonPaths = new ArrayList<>();
        List<ResultMapping> renameJsonPaths = new ArrayList<>();
        QueryExec queryExec = QueryExecFactory.of(new QueryExecStuff(selectValue, tableInfo), lookup,
                CommonStr.EMPTY_STRING,
                "$.*.",
                removeJsonPaths, renameJsonPaths);

        log.info("duration: time:{}", System.currentTimeMillis() - start);
        return new QueryExecLookup(queryExec, lookup, removeJsonPaths, renameJsonPaths);
    }

    // one time to get indexed and
    private QueryExec of(QueryExecStuff stuff, Map<String, QueryExec> indexed, String pre,
                         String preJsonPath,
                         List<String> removeJsonPaths, List<ResultMapping> renameJsonPaths) {

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
                .orElse(CommonStr.EMPTY_STRING);

        if (Objects.nonNull(queryExec.getRelation())) {
            indexed.put(needPre, queryExec);
        }


        TokenUtils.splitByComma(stuff.select()).parallelStream().forEach(selectItem -> {
            MTokens.SELECT_WITH_SUB.keyValue(selectItem)
                    .ifPresentOrElse(it -> {
                        QueryExec subQueryExec = QueryExecFactory.of(
                                SelectUtils.queryExecStuff(it, tableInfo),
                                indexed, needPre,
                                preJsonPath + it.key() + ".*."
                                , removeJsonPaths, renameJsonPaths);
                        queryExec.addSub(subQueryExec);
                        Optional.ofNullable(subQueryExec.getRelation())
                                .ifPresent(rel -> queryExec.putSubRelMap(subQueryExec.getRelEnd(), rel));
                    }, () -> {
                        queryExec.addQueryColumn(SelectUtils.queryColumn(selectItem, tableInfo));
                    });


        });

        final Set<String> allKeys = CacheTableInfoUtils.allColumnsWithRel(queryExec.getQueryTable());
        final Set<String> pickKey = queryExec.getPickKey();

        final Set<String> collect = allKeys.stream().filter(e -> !pickKey.contains(e))
                .map(it -> preJsonPath + it)
                .collect(Collectors.toSet());
        removeJsonPaths.addAll(collect);

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

}
