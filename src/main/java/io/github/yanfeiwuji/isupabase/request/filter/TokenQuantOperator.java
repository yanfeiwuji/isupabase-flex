package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryWrapper;

import io.github.yanfeiwuji.isupabase.request.utils.ExchangeUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

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
            (f, q) -> TokenQuantOperator.apply(f, q, qw -> qw::eq, qw -> qw::ne)),
    GTE("gte",
            (f, q) -> TokenQuantOperator.apply(f, q, qw -> qw::ge, qw -> qw::lt)),
    GT("gt",
            (f, q) -> TokenQuantOperator.apply(f, q, qw -> qw::gt, qw -> qw::le)),
    LET("lte",
            (f, q) -> TokenQuantOperator.apply(f, q, qw -> qw::le, qw -> qw::gt)),
    LT("lt",
            (f, q) -> TokenQuantOperator.apply(f, q, qw -> qw::lt, qw -> qw::ge)),
    LIKE("like",
            (f, q) -> TokenQuantOperator.apply(f, q, qw -> qw::like, qw -> qw::notLike)),
    ILIKE("ilike",
            (f, q) -> TokenQuantOperator.apply(f, q, qw -> qw::notLike, qw -> qw::like)),
    MATCH("match",
            (f, q) -> TokenQuantOperator.apply(f, q, qw -> qw::like, qw -> qw::notLike)),
    IMATCH("imatch",
            (f, q) -> TokenQuantOperator.apply(f, q, qw -> qw::notLike, qw -> qw::like));

    private String mark;
    private BiConsumer<Filter, QueryWrapper> handlerFunc;

    private static void apply(Filter filter,
                              QueryWrapper queryWrapper,
                              Function<QueryWrapper, BiConsumer<String, Object>> positiveFunc,
                              Function<QueryWrapper, BiConsumer<String, Object>> negativeFunc

    ) {
        Function<QueryWrapper, BiConsumer<String, Object>> exFunc = filter.isNegative() ? negativeFunc : positiveFunc;
        TokenModifiers modifiers = filter.getModifiers();
        List<Object> quantValue = filter.getQuantValue();
        Consumer<QueryWrapper> quantConsumer = qw -> quantValue.forEach(v -> exFunc.apply(qw).accept(filter.getRealColumn(), v));
        switch (modifiers) {
            case NONE -> exFunc.apply(queryWrapper).accept(filter.getRealColumn(), filter.getValue());
            case ALL -> queryWrapper.and(quantConsumer);
            case ANY -> queryWrapper.or(quantConsumer);
        }
    }

}
