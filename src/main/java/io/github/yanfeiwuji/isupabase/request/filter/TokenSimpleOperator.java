package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.yanfeiwuji.isupabase.request.utils.ExchangeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.BiConsumer;

@Getter
@AllArgsConstructor
public enum TokenSimpleOperator implements IOperator {
    NEQ("neq",
            (f, q) -> q.ne(f.getRealColumn(), f.getValue())),
    CS("cs",
            (f, q) -> q.and("@> ?", f.getValue())),
    CD("cd",
            (f, q) -> q.and("<@ ?", f.getValue())),
    OV("ov",
            (f, q) -> q.and("&& ?", f.getValue())),
    SL("sl",
            (f, q) -> q.and("<< ?", f.getValue())),
    SR("sr",
            (f, q) -> q.and(">> ?", f.getValue())),
    NXL("nxl",
            (f, q) -> q.and("&< ?", f.getValue())),
    NXR("nxr",
            (f, q) -> q.and("&> ?", f.getValue())),
    ADJ("adj",
            (f, q) -> q.and("-|- ?", f.getValue()));

    private String mark;
    private BiConsumer<Filter, QueryWrapper> handlerFunc;

}
