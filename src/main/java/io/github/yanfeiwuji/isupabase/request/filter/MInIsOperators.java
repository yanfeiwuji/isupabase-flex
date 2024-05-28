package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.*;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class MInIsOperators {
    public final Operator IN = new Operator("in", Pattern.compile("^in\\.\\((.*)\\)$"),
            f -> f.isNegative() ? f.getQueryColumn().notIn(f.getQuantValue())
                    : f.getQueryColumn().in(f.getQuantValue()));

    public final Operator IS = new Operator("is",
            Pattern.compile("^is\\.(null|true|false|unknown)$"),
            MInIsOperators::handlerIs);

    private QueryCondition handlerIs(Filter f) {
        String logic = f.isNegative() ? " IS NOT " : " IS ";
        return QueryCondition.create(f.getQueryColumn(), logic, new RawQueryColumn(f.getStrValue()));
    }
}
