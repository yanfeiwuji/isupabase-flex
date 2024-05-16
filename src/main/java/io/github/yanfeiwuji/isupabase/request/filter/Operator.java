package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;

public record Operator(String mark, Pattern pattern,
                       Function<Filter, QueryCondition> handler)
        implements IToken {

}
