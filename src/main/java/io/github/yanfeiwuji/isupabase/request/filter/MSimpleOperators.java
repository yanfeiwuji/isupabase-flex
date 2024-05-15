package io.github.yanfeiwuji.isupabase.request.filter;

import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class MSimpleOperators {
    public final Operator NEQ = new Operator("neq", TokenUtils.opDot("neq"),
            (f, q) -> q.ne(f.getRealColumn(), f.getValue()));
    public final Operator CS = new Operator("cs", TokenUtils.opDot("cs"),
            (f, q) -> q.and("@> ?", f.getValue()));
    public final Operator CD = new Operator("cd", TokenUtils.opDot("cd"),
            (f, q) -> q.and("<@ ?", f.getValue()));
    public final Operator OV = new Operator("ov", TokenUtils.opDot("ov"),
            (f, q) -> q.and("&& ?", f.getValue()));
    public final Operator SL = new Operator("sl", TokenUtils.opDot("sl"),
            (f, q) -> q.and("<< ?", f.getValue()));
    public final Operator SR = new Operator("sr", TokenUtils.opDot("sr"),
            (f, q) -> q.and(">> ?", f.getValue()));
    public final Operator NXL = new Operator("nxl", TokenUtils.opDot("nxl"),
            (f, q) -> q.and("&< ?", f.getValue()));
    public final Operator NXR = new Operator("nxr", TokenUtils.opDot("nxr"),
            (f, q) -> q.and("&> ?", f.getValue()));
    public final Operator ADJ = new Operator("adj", TokenUtils.opDot("adj"),
            (f, q) -> q.and("-|- ?", f.getValue()));
}
