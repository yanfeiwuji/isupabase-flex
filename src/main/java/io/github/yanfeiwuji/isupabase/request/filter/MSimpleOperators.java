package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

@UtilityClass
public final class MSimpleOperators {
    public final Operator NEQ = new Operator("neq", TokenUtils.opDot("neq"),
            f -> f.getQueryColumn().ne(f.getValue()));
    public final Operator CS = new Operator("cs", TokenUtils.opDot("cs"),
            MSimpleOperators.simple(" @> "));
    public final Operator CD = new Operator("cd", TokenUtils.opDot("cd"),
            MSimpleOperators.simple(" <@ "));
    public final Operator OV = new Operator("ov", TokenUtils.opDot("ov"),
            MSimpleOperators.simple(" && "));
    public final Operator SL = new Operator("sl", TokenUtils.opDot("sl"),
            MSimpleOperators.simple(" << "));
    public final Operator SR = new Operator("sr", TokenUtils.opDot("sr"),
            MSimpleOperators.simple(" >> "));
    public final Operator NXL = new Operator("nxl", TokenUtils.opDot("nxl"),
            MSimpleOperators.simple(" &< "));
    public final Operator NXR = new Operator("nxr", TokenUtils.opDot("nxr"),
            MSimpleOperators.simple(" &> "));
    public final Operator ADJ = new Operator("adj", TokenUtils.opDot("adj"),
            MSimpleOperators.simple(" -|- "));

    private Function<Filter, QueryCondition> simple(String logic) {
        return f -> QueryCondition.create(f.getQueryColumn(), logic, f.getValue());
    }
}
