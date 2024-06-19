package io.github.yanfeiwuji.isupabase.request.select;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.mybatisflex.core.constant.SqlOperator;
import com.mybatisflex.core.query.*;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import io.github.yanfeiwuji.isupabase.request.utils.ValueUtils;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class QueryConditionFactory {

    private static final Map<String, Function<List<QueryCondition>, QueryCondition>> LOGIC_OP_MAP = Map.of(
            PgrstStrPool.AND,
            queryConditions -> queryConditions.stream().reduce(QueryCondition.createEmpty(), QueryCondition::and),
            PgrstStrPool.NOT_AND,
            queryConditions -> QueryMethods
                    .not(queryConditions.stream().reduce(QueryCondition.createEmpty(), QueryCondition::and)),
            PgrstStrPool.OR,
            queryConditions -> queryConditions.stream().reduce(QueryCondition.createEmpty(), QueryCondition::or),
            PgrstStrPool.NOT_OR,
            queryConditions -> QueryMethods
                    .not(queryConditions.stream().reduce(QueryCondition.createEmpty(), QueryCondition::or)));

    private static final Set<String> ALLOW_MODIFIERS = Stream
            .of("eq", "like", "ilike", "gt", "gte", "lt", "lte", "match", "imatch")
            .collect(Collectors.toSet());

    // QueryColumn queryColumn, String value
    // default single
    public static final Map<String, BiFunction<QueryColumn, String, QueryCondition>> OP_BIFUNC_MAP = MapUtil
            .<String, BiFunction<QueryColumn, String, QueryCondition>>builder()
            .put("eq", QueryConditionFactory::eq)
            .put("gt", QueryConditionFactory::gt)
            .put("gte", QueryConditionFactory::gte)
            .put("lt", QueryConditionFactory::lt)
            .put("lte", QueryConditionFactory::lte)
            .put("neq", QueryConditionFactory::neq)
            .put("like", QueryConditionFactory::like)
            .put("ilike", QueryConditionFactory::ilike)
            .put("in", QueryConditionFactory::in)
            .put("is", QueryConditionFactory::is)
            .put("match", QueryConditionFactory::match)
            .put("imatch", QueryConditionFactory::imatch)
            .put("in", QueryConditionFactory::in)
            .put("isdistinct", QueryConditionFactory::isdistinct)
            .put("fts", QueryConditionFactory::fts)
            .put("plfts", QueryConditionFactory::plfts)
            .put("phfts", QueryConditionFactory::phfts)
            .put("wfts", QueryConditionFactory::wfts)
            .put("cs", QueryConditionFactory::cs)
            .put("cd", QueryConditionFactory::cd)
            .put("ov", QueryConditionFactory::ov)
            .put("sl", QueryConditionFactory::sl)
            .put("sr", QueryConditionFactory::sr)
            .put("nxr", QueryConditionFactory::nxr)
            .put("nxl", QueryConditionFactory::nxl)
            .put("adj", QueryConditionFactory::adj)
            .build();

    // has column then handler value
    public QueryCondition ofNoLogic(QueryColumn queryColumn, String value) {
        // not.op(op).value
        QueryCondition queryCondition = MTokens.OP_VALUE.keyValue(value)
                .map(kv -> {
                    if (kv.key().endsWith(PgrstStrPool.MODIFIER_ALL)) {
                        String op = CharSequenceUtil.removeSuffix(kv.key(), PgrstStrPool.MODIFIER_ALL);
                        return handler(queryColumn, kv.value(), op, PgrstStrPool.MODIFIER_ALL);
                    } else if (kv.key().endsWith(PgrstStrPool.MODIFIER_ANY)) {
                        String op = CharSequenceUtil.removeSuffix(kv.key(), PgrstStrPool.MODIFIER_ANY);
                        return handler(queryColumn, kv.value(), op, PgrstStrPool.MODIFIER_ANY);
                    } else {
                        return handler(queryColumn, kv.value(), kv.key(), null);
                    }
                }).orElseThrow(PgrstExFactory.exParseFilterError(value));
        return value.startsWith(PgrstStrPool.NOT_DOT) ? QueryMethods.not(queryCondition) : queryCondition;
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
                                            .orElseThrow(PgrstExFactory.exParseLogicTreeError(value))))
                            .toList();
                    return op.apply(list);
                }).orElseGet(() -> QueryConditionFactory.ofNoLogic(tableInfo, key, value));
    }


    private QueryCondition handler(QueryColumn queryColumn, String value, String op, String modifier) {

        BiFunction<QueryColumn, String, QueryCondition> biFunction = Optional.ofNullable(OP_BIFUNC_MAP.get(op))
                .orElseThrow(PgrstExFactory.exParseFilterError(value));

        if (Objects.nonNull(modifier)) {
            if (!ALLOW_MODIFIERS.contains(op)) {
                throw PgrstExFactory.exParseFilterError(op).get();
            } else {
                if (CharSequenceUtil.equals(PgrstStrPool.MODIFIER_ALL, modifier)) {
                    return TokenUtils.splitByCommaQuoted(TokenUtils.removeDelim(value))
                            .stream().map(v -> biFunction.apply(queryColumn, v))
                            .reduce(QueryCondition.createEmpty(), QueryCondition::and);
                } else if (CharSequenceUtil.equals(PgrstStrPool.MODIFIER_ANY, modifier)) {
                    return TokenUtils.splitByCommaQuoted(TokenUtils.removeDelim(value))
                            .stream().map(v -> biFunction.apply(queryColumn, v))
                            .reduce(QueryCondition.createEmpty(), QueryCondition::or);
                } else {
                    // not handler
                    return QueryCondition.createEmpty();
                }
            }
        } else {
            return biFunction.apply(queryColumn, value);
        }
    }

    private QueryCondition eq(QueryColumn queryColumn, String value) {
        return queryColumn.eq(ValueUtils.singleValue(queryColumn, value));
    }

    private QueryCondition gt(QueryColumn queryColumn, String value) {
        return queryColumn.gt(ValueUtils.singleValue(queryColumn, value));
    }

    private QueryCondition gte(QueryColumn queryColumn, String value) {
        return queryColumn.ge(ValueUtils.singleValue(queryColumn, value));
    }

    private QueryCondition lt(QueryColumn queryColumn, String value) {
        return queryColumn.lt(ValueUtils.singleValue(queryColumn, value));
    }

    private QueryCondition lte(QueryColumn queryColumn, String value) {
        return queryColumn.le(ValueUtils.singleValue(queryColumn, value));
    }

    private QueryCondition neq(QueryColumn queryColumn, String value) {
        return queryColumn.ne(ValueUtils.singleValue(queryColumn, value));
    }

    private QueryCondition like(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, SqlOperator.LIKE, ValueUtils.likeValue(queryColumn, value));
    }

    private static QueryCondition ilike(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, SqlOperator.NOT_LIKE, ValueUtils.likeValue(queryColumn, value));
    }

    private static QueryCondition in(QueryColumn queryColumn, String value) {
        return queryColumn.in(ValueUtils.listValueParentheses(queryColumn, value));
    }

    private static QueryCondition is(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.IS_SQL_OP,
                new RawQueryColumn(ValueUtils.isValue(queryColumn, value)));
    }

    private QueryCondition match(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.MATCH_SQL_OP, ValueUtils.singleValue(queryColumn, value));
    }

    private QueryCondition adj(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.ADJ_SQL_OP, ValueUtils.singleValue(queryColumn, value));
    }

    private static QueryCondition nxl(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.NXL_SQL_OP, ValueUtils.singleValue(queryColumn, value));
    }

    private static QueryCondition nxr(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.NXR_SQL_OP, ValueUtils.singleValue(queryColumn, value));
    }

    private static QueryCondition sr(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.SR_SQL_OP, ValueUtils.singleValue(queryColumn, value));
    }

    private static QueryCondition sl(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.SL_SQL_OP, ValueUtils.singleValue(queryColumn, value));
    }

    private static QueryCondition ov(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.OV_SQL_OP, ValueUtils.singleValue(queryColumn, value));
    }

    private static QueryCondition cd(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.CD_SQL_OP, ValueUtils.singleValue(queryColumn, value));
    }

    private static QueryCondition cs(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.CS_SQL_OP, ValueUtils.singleValue(queryColumn, value));
    }

    private static QueryCondition wfts(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.WFTS_SQL_OP, ValueUtils.singleValue(queryColumn, value));
    }

    private static QueryCondition phfts(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.PHFTS_SQL_OP, ValueUtils.singleValue(queryColumn, value));
    }

    private static QueryCondition plfts(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.PLFTS_SQL_OP, ValueUtils.singleValue(queryColumn, value));
    }

    private static QueryCondition fts(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.FTS_SQL_OP, ValueUtils.singleValue(queryColumn, value));
    }

    private static QueryCondition isdistinct(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.ISDISTINCT_SQL_OP,
                ValueUtils.singleValue(queryColumn, value));
    }

    private static QueryCondition imatch(QueryColumn queryColumn, String value) {
        return QueryCondition.create(queryColumn, PgrstStrPool.IMATCH_SQL_OP, ValueUtils.singleValue(queryColumn, value));
    }

}
