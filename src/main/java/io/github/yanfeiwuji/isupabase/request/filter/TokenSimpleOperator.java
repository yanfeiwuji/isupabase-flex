package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.yanfeiwuji.isupabase.request.utils.ExchangeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@Getter
@AllArgsConstructor
public enum TokenSimpleOperator implements IOperator {
    NEQ("neq",
            (f, q) -> q.ne(f.getRealColumn(), f.getValue()),
            TokenSimpleOperator::calcValue
    ),
    CS("cs",
            (f, q) -> q.and("@> ?", f.getValue()),
            TokenSimpleOperator::calcValue
    ),
    CD("cd",
            (f, q) -> q.and("<@ ?", f.getValue()),
            TokenSimpleOperator::calcValue
    ),
    OV("ov",
            (f, q) -> q.and("&& ?", f.getValue()),
            TokenSimpleOperator::calcValue
    ),
    SL("sl",
            (f, q) -> q.and("<< ?", f.getValue()),
            TokenSimpleOperator::calcValue
    ),
    SR("sr",
            (f, q) -> q.and(">> ?", f.getValue()),
            TokenSimpleOperator::calcValue
    ),
    NXL("nxl",
            (f, q) -> q.and("&< ?", f.getValue()),
            TokenSimpleOperator::calcValue
    ),
    NXR("nxr",
            (f, q) -> q.and("&> ?", f.getValue()),
            TokenSimpleOperator::calcValue
    ),
    ADJ("adj",
            (f, q) -> q.and("-|- ?", f.getValue()),
            TokenSimpleOperator::calcValue
    );
    private String mark;
    private BiFunction<Filter, QueryWrapper, QueryWrapper> handlerFunc;
    private BiConsumer<Filter, IOperator> calcValueFunc;


    public static void calcValue(Filter filter, IOperator op) {
        String paramValue = filter.getParamValue();
        filter.setStrValue(IOperator.removePre(paramValue, op));
        Object o = ExchangeUtils.singleValue(filter);
        filter.setValue(o);
    }

}
