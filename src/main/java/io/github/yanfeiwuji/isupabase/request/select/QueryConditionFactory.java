package io.github.yanfeiwuji.isupabase.request.select;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.constants.CommonStr;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@UtilityClass
public class QueryConditionFactory {

    private static final Map<String, Function<List<QueryCondition>, QueryCondition>>
            LOGIC_OP_MAP =
            Map.of(
                    CommonStr.AND,
                    queryConditions -> queryConditions.stream().reduce(QueryCondition.createEmpty(), QueryCondition::and),
                    CommonStr.NOT_AND,
                    queryConditions -> QueryMethods.not(queryConditions.stream().reduce(QueryCondition.createEmpty(), QueryCondition::and)),
                    CommonStr.OR,
                    queryConditions -> queryConditions.stream().reduce(QueryCondition.createEmpty(), QueryCondition::or),
                    CommonStr.NOT_OR,
                    queryConditions -> QueryMethods.not(queryConditions.stream().reduce(QueryCondition.createEmpty(), QueryCondition::or))
            );


    // has column then handler value
    public QueryCondition ofNoLogic(QueryColumn queryColumn, String value) {
        // not.op(op).value

        boolean isboot = false;

        return QueryCondition.create(queryColumn, value);
    }

    public QueryCondition ofNoLogic(TableInfo tableInfo, String key, String value) {
        return QueryConditionFactory.ofNoLogic(CacheTableInfoUtils.nNRealQueryColumn(key, tableInfo), value);
    }

    public QueryCondition of(TableInfo tableInfo, String key, List<String> values) {
        return values.stream().map(value -> QueryConditionFactory.of(tableInfo, key, value))
                .reduce(QueryCondition.createEmpty(), QueryCondition::and);
    }

    public QueryCondition of(TableInfo tableInfo, String key, String value) {

        return Optional.ofNullable(LOGIC_OP_MAP.get(key))
                .map(op -> {
                    List<QueryCondition> list = TokenUtils.splitByComma(TokenUtils.removeRoundBrackets(value))
                            .stream().map(it -> MTokens.INNER_LOGIC
                                    .keyValue(it)
                                    .map(kv -> QueryConditionFactory.of(tableInfo, kv.key(), kv.value()))
                                    .orElseGet(() -> MTokens.KEY_DOT_VALUE.keyValue(it)
                                            .map(kv -> QueryConditionFactory.ofNoLogic(tableInfo, kv.key(), kv.value()))
                                            // todo add exception no handler logic tree
                                            .orElseThrow())
                            ).toList();
                    return op.apply(list);
                }).orElseGet(() ->
                        QueryConditionFactory.ofNoLogic(tableInfo, key, value)
                );
    }

}
