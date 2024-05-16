package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.constant.SqlOperator;
import com.mybatisflex.core.query.*;
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
        // todo handler other param
        if (f.isNegative()) {
            QueryColumn queryColumn = f.getQueryColumn();


            return QueryColumnBehavior.castCondition(QueryCondition.create(
                    queryColumn, " is not %s ".formatted(f.getStrValue()), null).when(true));
        } else {
            return QueryColumnBehavior.castCondition(QueryCondition.create(
                    f.getQueryColumn(),
                    " is %s ".formatted(f.getStrValue()),
                    null
            ).when(true));
        }
    }
}
