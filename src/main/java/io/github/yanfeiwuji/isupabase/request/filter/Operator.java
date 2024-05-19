package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryCondition;
import java.util.function.Function;
import java.util.regex.Pattern;

public record Operator(String mark, Pattern pattern,
        Function<Filter, QueryCondition> handler)
        implements IToken {

}
