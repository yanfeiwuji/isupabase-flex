package io.github.yanfeiwuji.isupabase.request.select;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryOrderBy;
import com.mybatisflex.core.relation.AbstractRelation;
import com.mybatisflex.core.relation.ManyToMany;
import com.mybatisflex.core.relation.OneToMany;
import com.mybatisflex.core.table.TableInfo;

import cn.hutool.core.util.NumberUtil;
import io.github.yanfeiwuji.isupabase.constants.CommonLambda;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QueryExecAssembleManager {

    private static final Map<String, BiConsumer<QueryExec, List<String>>> LIMIT_OFFSET_ORDER_MAP = Map.of(
            CommonStr.LIMIT, QueryExecAssembleManager::assembleLimit,
            CommonStr.OFFSET, QueryExecAssembleManager::assembleOffset,
            CommonStr.ORDER, QueryExecAssembleManager::assembleOrder,
            CommonStr.SELECT, CommonLambda::emptyQueryExecAssembly,
            CommonStr.COLUMNS, CommonLambda::emptyQueryExecAssembly);

    public Optional<BiConsumer<QueryExec, List<String>>> assembleLimitOffsetOrder(String key) {
        return Optional.ofNullable(LIMIT_OFFSET_ORDER_MAP.get(key));
    }

    private void assembleLimit(QueryExec queryExec, List<String> values) {
        queryExec.setLimit(NumberUtil.parseNumber(values.getFirst()));
    }

    private void assembleOffset(QueryExec queryExec, List<String> values) {
        queryExec.setOffset(NumberUtil.parseNumber(values.getFirst()));
    }

    public void assembleFilter(QueryExec queryExec, String key, List<String> values) {
        TableInfo tableInfo = queryExec.getTableInfo();
        queryExec.getQueryCondition().and(QueryConditionFactory.of(tableInfo, key, values));

    }

    private void assembleOrder(QueryExec queryExec, List<String> values) {
        values.forEach(it -> assembleOrderString(queryExec, it));
    }

    private void assembleOrderString(QueryExec queryExec, String orderString) {
        CharSequenceUtil.split(orderString, StrPool.COMMA)
                .forEach(it -> assembleOrderSingle(queryExec, it));
    }

    private void assembleOrderSingle(QueryExec queryExec, String value) {
        List<String> groups = MTokens.ORDER_BY.groups(value);

        if (groups.size() != 5) {
            throw PgrstExFactory.exParseOrderError(value).get();
        }
        String one = groups.get(1);
        String two = groups.get(2);
        QueryColumn queryColumn;
        if (two != null) {
            AbstractRelation<?> relation = Optional.ofNullable(queryExec.getSubRelMap())
                    .map(it -> it.get(one))
                    .orElseThrow(PgrstExFactory.exEmbeddedApplyButNotInSelect(one));
            if (relation instanceof OneToMany<?> || relation instanceof ManyToMany<?>) {
                throw PgrstExFactory.exCanNotOrderRelForManyEnd(queryExec.getTableInfo().getTableName(), one).get();
            }
            queryExec.addJoin(relation);
            TableInfo targetTableInfo = relation.getTargetTableInfo();
            queryColumn = CacheTableInfoUtils.nNRealQueryColumn(two, targetTableInfo);
        } else {
            queryColumn = CacheTableInfoUtils.nNRealQueryColumn(one, queryExec.getTableInfo());
        }
        QueryOrderBy queryOrderBy = QueryOrderByFactory.of(queryColumn, groups.get(3), groups.get(4));
        queryExec.addOrder(queryOrderBy);

    }

}
