package io.github.yanfeiwuji.isupabase.request.select;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.table.TableInfo;

import cn.hutool.core.util.NumberUtil;
import io.github.yanfeiwuji.isupabase.constants.CommonLambda;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QueryExecAssembleManager {

    private static final Map<String, BiConsumer<QueryExec, List<String>>>
            LIMIT_OFFSET_ORDER_MAP =
            Map.of(
                    CommonStr.LIMIT, QueryExecAssembleManager::assembleLimit,
                    CommonStr.OFFSET, QueryExecAssembleManager::assembleOffset,
                    CommonStr.ORDER, QueryExecAssembleManager::assembleOrder,
                    CommonStr.SELECT, CommonLambda::emptyQueryExecAssembly
            );


    public Optional<BiConsumer<QueryExec, List<String>>> assembleLimitOffsetOrder(String key) {
        return Optional.ofNullable(LIMIT_OFFSET_ORDER_MAP.get(key));
    }

    private void assembleLimit(QueryExec queryExec, List<String> values) {
        queryExec.setLimit(NumberUtil.parseNumber(values.getFirst()));
    }

    private void assembleOffset(QueryExec queryExec, List<String> values) {

        queryExec.setOffset(NumberUtil.parseNumber(values.getFirst()));
    }

    private void assembleOrder(QueryExec queryExec, List<String> values) {

        // queryExec.setOffset(NumberUtil.parseNumber(values.getFirst()));
    }

    public void assembleFilter(QueryExec queryExec, String key, List<String> values) {
        TableInfo tableInfo = queryExec.getTableInfo();
        queryExec.setQueryCondition(QueryConditionFactory.of(tableInfo, key, values));
    }

    private QueryCondition assembleSingleFilter(QueryColumn queryColumn, String value) {
        //
        return QueryCondition.create(queryColumn, value);
    }


    private void handler(QueryExec queryExec, String key, String value) {


    }

    private void assembleOr(QueryCondition queryCondition, List<String> values) {

    }
}
