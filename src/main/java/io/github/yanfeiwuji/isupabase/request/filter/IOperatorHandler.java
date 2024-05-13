package io.github.yanfeiwuji.isupabase.request.filter;


import com.mybatisflex.core.query.QueryWrapper;


@FunctionalInterface
public interface IOperatorHandler {


   QueryWrapper handler(Filter filter, QueryWrapper queryWrapper);

}
