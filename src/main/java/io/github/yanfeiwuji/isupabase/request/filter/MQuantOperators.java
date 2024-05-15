package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@UtilityClass
public class MQuantOperators {
    public final Operator EQ = new Operator("eq", TokenUtils.quantOp("eq"),
            (f, q) -> MQuantOperators.apply(f, q, qw -> qw::eq, qw -> qw::ne));
    public final Operator GTE = new Operator("gte", TokenUtils.quantOp("gte"),
            (f, q) -> MQuantOperators.apply(f, q, qw -> qw::ge, qw -> qw::lt));
    public final Operator GT = new Operator("gt", TokenUtils.quantOp("gt"),
            (f, q) -> MQuantOperators.apply(f, q, qw -> qw::gt, qw -> qw::le));

    public final Operator LTE = new Operator("lte", TokenUtils.quantOp("lte"),
            (f, q) -> MQuantOperators.apply(f, q, qw -> qw::le, qw -> qw::gt));
    public final Operator LT = new Operator("lt", TokenUtils.quantOp("lt"),
            (f, q) -> MQuantOperators.apply(f, q, qw -> qw::lt, qw -> qw::ge));
    public final Operator LIKE = new Operator("like", TokenUtils.quantOp("like"),
            (f, q) -> MQuantOperators.apply(f, q, qw -> qw::like, qw -> qw::notLike));

    public final Operator ILIKE = new Operator("ilike", TokenUtils.quantOp("ilike"),
            (f, q) -> MQuantOperators.apply(f, q, qw -> qw::notLike, qw -> qw::like));

    public final Operator MATCH = new Operator("match", TokenUtils.quantOp("match"),
            (f, q) -> MQuantOperators.apply(f, q, qw -> qw::like, qw -> qw::notLike));
    public final Operator IMATCH = new Operator("eq", TokenUtils.quantOp("imatch"),
            (f, q) -> MQuantOperators.apply(f, q, qw -> qw::notLike, qw -> qw::like));


    private void apply(Filter filter,
                       QueryWrapper queryWrapper,
                       Function<QueryWrapper, BiConsumer<String, Object>> positiveFunc,
                       Function<QueryWrapper, BiConsumer<String, Object>> negativeFunc

    ) {
        Function<QueryWrapper, BiConsumer<String, Object>> exFunc = filter.isNegative() ? negativeFunc
                : positiveFunc;
        Modifier modifiers = filter.getModifier();
        List<Object> quantValue = filter.getQuantValue();
        Consumer<QueryWrapper> quantConsumer = qw -> quantValue
                .forEach(v -> exFunc.apply(qw).accept(filter.getRealColumn(), v));
        switch (modifiers) {
            case Modifier.none -> exFunc.apply(queryWrapper).accept(filter.getRealColumn(), filter.getValue());
            case Modifier.all  -> queryWrapper.and(quantConsumer);
            case  Modifier.any -> queryWrapper.or(quantConsumer);
        }
    }
}
