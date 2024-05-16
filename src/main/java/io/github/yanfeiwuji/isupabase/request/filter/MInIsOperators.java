package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class MInIsOperators {
    public final Operator IN = new Operator("in", Pattern.compile("^in\\.\\((.*)\\)$"),
            f -> f.isNegative() ?
                    f.getQueryColumn().notIn(f.getQuantValue()) :
                    f.getQueryColumn().in(f.getQuantValue()));

    public final Operator IS = new Operator("is", Pattern.compile("^is\\.(null|true|false|unknown)$"),
            MInIsOperators::handlerIs
    );

    private QueryCondition handlerIs(Filter f) {
        if (f.isNegative()) {
            return QueryCondition.create(f.getQueryColumn(), " is not %s ".formatted(f.getStrValue()));
        } else {
            return QueryCondition.create(f.getQueryColumn(), " is %s ".formatted(f.getStrValue()));
        }
    }
}
