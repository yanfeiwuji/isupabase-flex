package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryCondition;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Pattern;

@UtilityClass
@Slf4j
public class MLogicalOperators {
    public final Operator AND = new Operator("and", Pattern.compile("^and(.*)"),
            MLogicalOperators.apply(QueryCondition::and)

    );
    public final Operator OR = new Operator("or", Pattern.compile("^or(.*)"),
            MLogicalOperators.apply(QueryCondition::or)
    );

    public Function<Filter, QueryCondition> apply(BinaryOperator<QueryCondition> binaryOperator) {
        return f -> QueryCondition.createEmpty()

                .and(f.getFilters().stream().map(Filter::toQueryCondition)
                .reduce(binaryOperator)
                .orElse(QueryCondition.createEmpty()));
    }
}
