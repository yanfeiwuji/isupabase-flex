package io.github.yanfeiwuji.isupabase.request.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;

import io.github.yanfeiwuji.isupabase.request.IReqQueryWrapperHandler;
import io.github.yanfeiwuji.isupabase.request.filter.Filter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.ServerRequest;

@Component
@AllArgsConstructor
public class ReqQueryWrapperHandler implements IReqQueryWrapperHandler {

    @Override
    public QueryWrapper handler(ServerRequest request, TableInfo tableInfo) {
        MultiValueMap<String, String> params = request.params();
        QueryWrapper wrapper = QueryWrapper.create();
        handlerHorizontalFilter(params, tableInfo, wrapper);
        return wrapper;
    }

    public QueryWrapper handlerHorizontalFilter(MultiValueMap<String, String> params, TableInfo tableInfo,
            QueryWrapper queryWrapper) {
        params.forEach((key, value) -> {
            value.forEach(it -> new Filter(key, it, tableInfo).handler(queryWrapper));
        });
        return queryWrapper;
    }

}
