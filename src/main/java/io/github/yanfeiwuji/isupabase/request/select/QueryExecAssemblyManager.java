package io.github.yanfeiwuji.isupabase.request.select;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import com.mybatisflex.core.constant.SqlOperator;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.table.TableInfo;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QueryExecAssemblyManager {

    private static final Map<String, BiConsumer<QueryExec, List<String>>> map = MapUtil
            .<String, BiConsumer<QueryExec, List<String>>>builder(new ConcurrentHashMap<>())
            .put(CommonStr.LIMIT, QueryExecAssemblyManager::assembleLimit)
            .put(CommonStr.OFFSET, QueryExecAssemblyManager::assembleOffset)
            .put(CommonStr.ORDER, QueryExecAssemblyManager::assembleOrder)
            // empty cache select but not any operator
            .put(CommonStr.SELECT,
                    (exec, values) -> {
                    })
            .build();

    public Optional<BiConsumer<QueryExec, List<String>>> assembleLimitOffsetOrder(String key) {
        return Optional.ofNullable(map.get(key));
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

        QueryColumn queryColumn = CacheTableInfoUtils.nNRealQueryColumn(key, tableInfo);
        // TODO handler sub
        QueryMethods.not(QueryCondition.create(queryColumn, SqlOperator.EQUALS, ""));

        // System.out.println(key + "=" + queryExec.getTableInfo().getTableName());
        // // queryExec.setOffset(NumberUtil.parseNumber(values.getFirst()));
    }
}
