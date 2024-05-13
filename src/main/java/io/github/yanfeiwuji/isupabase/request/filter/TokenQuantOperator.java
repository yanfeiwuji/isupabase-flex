package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * = OpEqual eq
 * | OpGreaterThanEqual gte
 * | OpGreaterThan gt
 * | OpLessThanEqual lte
 * | OpLessThan lt
 * | OpLike like
 * | OpILike ilike
 * | OpMatch  match
 * | OpIMatch imatch
 */
@Getter
@AllArgsConstructor
public enum TokenQuantOperator implements IOperator {
    EQ("eq",
            (f, q) -> q.eq(f.getRealColumn(), f.getValue()).eq(f.getRealColumn(), f.getValue()),
            IOperator::defaultCalcValue
    ),
    GTE("gte",
            (f, q) -> q.ge(f.getRealColumn(), f.getValue()),
            IOperator::defaultCalcValue
    ),
    GT("gt",
            (f, q) -> q.gt(f.getRealColumn(), f.getValue()),
            IOperator::defaultCalcValue
    ),
    LET("lte",
            (f, q) -> q.le(f.getRealColumn(), f.getValue()),
            IOperator::defaultCalcValue
    ),
    LT("lt",
            (f, q) -> q.lt(f.getRealColumn(), f.getValue()),
            IOperator::defaultCalcValue
    ),
    LIKE("like",
            (f, q) -> q.like(f.getRealColumn(), f.getValue()),
            IOperator::defaultCalcValue
    ),
    ILIKE("ilike",
            (f, q) -> q.notLike(f.getRealColumn(), f.getValue()),
            IOperator::defaultCalcValue
    ),
    MATCH("match",
            (f, q) -> q.eq(f.getRealColumn(), f.getValue()),
            IOperator::defaultCalcValue
    ),
    IMATCH("imatch",
            (f, q) -> q.eq(f.getRealColumn(), f.getValue()),
            IOperator::defaultCalcValue
    );

    private String mark;
    private BiFunction<Filter, QueryWrapper, QueryWrapper> handlerFunc;
    private BiConsumer<Filter, IOperator> calcValueFunc;
}
