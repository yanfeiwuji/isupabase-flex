package io.github.yanfeiwuji.isupabase.request.filter;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface IOperator extends IToken {
    BiFunction<Filter, QueryWrapper, QueryWrapper> getHandlerFunc();

    BiConsumer<Filter, IOperator> getCalcValueFunc();

    default QueryWrapper defaultHandler(Filter filter, QueryWrapper queryWrapper) {
        return queryWrapper;
    }

    static void defaultCalcValue(Filter filter, IOperator operator) {

    }

    static String removePre(String paramValue, IOperator operator) {
        return paramValue.replaceFirst(operator.getMark() + StrUtil.DOT, "");
    }
}
