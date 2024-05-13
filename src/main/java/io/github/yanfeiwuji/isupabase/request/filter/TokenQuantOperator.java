package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryWrapper;

import io.github.yanfeiwuji.isupabase.request.utils.ExchangeUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * = OpEqual eq
 * | OpGreaterThanEqual gte
 * | OpGreaterThan gt
 * | OpLessThanEqual lte
 * | OpLessThan lt
 * | OpLike like
 * | OpILike ilike
 * | OpMatch match
 * | OpIMatch imatch
 */
@Getter
@AllArgsConstructor
public enum TokenQuantOperator implements IOperator {
    EQ("eq",
            (f, q) -> q.eq(f.getRealColumn(), f.getValue())),
    GTE("gte",
            (f, q) -> q.ge(f.getRealColumn(), f.getValue())),
    GT("gt",
            (f, q) -> q.gt(f.getRealColumn(), f.getValue())),
    LET("lte",
            (f, q) -> q.le(f.getRealColumn(), f.getValue())),
    LT("lt",
            (f, q) -> q.lt(f.getRealColumn(), f.getValue())),
    LIKE("like",
            (f, q) -> q.like(f.getRealColumn(), f.getValue())),
    ILIKE("ilike",
            (f, q) -> q.notLike(f.getRealColumn(), f.getValue())),
    MATCH("match",
            (f, q) -> q.eq(f.getRealColumn(), f.getValue())),
    IMATCH("imatch",
            (f, q) -> q.eq(f.getRealColumn(), f.getValue()));

    private String mark;
    private BiConsumer<Filter, QueryWrapper> handlerFunc;

}
