package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryWrapper;

import java.util.function.BiConsumer;

public interface IOperator extends IToken {
    BiConsumer<Filter, QueryWrapper> getHandlerFunc();

    default QueryWrapper defaultHandler(Filter filter, QueryWrapper queryWrapper) {
        return queryWrapper;
    }

}
