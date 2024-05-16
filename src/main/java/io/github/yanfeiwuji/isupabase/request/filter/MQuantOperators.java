package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class MQuantOperators {
    public final Operator EQ = new Operator("eq", TokenUtils.quantOp("eq"),
            f -> MQuantOperators.apply(f, f.getQueryColumn()::eq, f.getQueryColumn()::ne));
    public final Operator GTE = new Operator("gte", TokenUtils.quantOp("gte"),
            f -> MQuantOperators.apply(f, f.getQueryColumn()::ge, f.getQueryColumn()::lt));
    public final Operator GT = new Operator("gt", TokenUtils.quantOp("gt"),
            f -> MQuantOperators.apply(f, f.getQueryColumn()::gt, f.getQueryColumn()::le));
    public final Operator LTE = new Operator("lte", TokenUtils.quantOp("lte"),
            f -> MQuantOperators.apply(f, f.getQueryColumn()::le, f.getQueryColumn()::gt));
    public final Operator LT = new Operator("lt", TokenUtils.quantOp("lt"),
            f -> MQuantOperators.apply(f, f.getQueryColumn()::lt, f.getQueryColumn()::ge));
    public final Operator LIKE = new Operator("like", TokenUtils.quantOp("like"),
            f -> MQuantOperators.apply(f, f.getQueryColumn()::like, f.getQueryColumn()::notLike));
    public final Operator ILIKE = new Operator("ilike", TokenUtils.quantOp("ilike"),
            f -> MQuantOperators.apply(f, f.getQueryColumn()::notLike, f.getQueryColumn()::like));
    public final Operator MATCH = new Operator("match", TokenUtils.quantOp("match"),
            f -> MQuantOperators.apply(f, f.getQueryColumn()::like, f.getQueryColumn()::notLike));
    public final Operator IMATCH = new Operator("imatch", TokenUtils.quantOp("imatch"),
            f -> MQuantOperators.apply(f, f.getQueryColumn()::notLike, f.getQueryColumn()::like));

    private QueryCondition apply(Filter filter,
            Function<Object, QueryCondition> positiveFunc,
            Function<Object, QueryCondition> negativeFunc

    ) {

        Function<Object, QueryCondition> exFunc = filter.isNegative() ? negativeFunc
                : positiveFunc;
        EModifier modifiers = filter.getModifier();
        return switch (modifiers) {
            case EModifier.none -> exFunc.apply(filter.getValue());
            case EModifier.all -> filter.getQuantValue().stream().map(exFunc)
                    .reduce(QueryCondition::and)
                    .orElse(QueryCondition.createEmpty());
            case EModifier.any -> filter.getQuantValue().stream().map(exFunc)
                    .reduce(QueryCondition::or)
                    .orElse(QueryCondition.createEmpty());
        };
    }
}
