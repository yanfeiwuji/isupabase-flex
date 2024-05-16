package io.github.yanfeiwuji.isupabase.request.impl;

import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;

import io.github.yanfeiwuji.isupabase.request.IReqQueryWrapperHandler;
import io.github.yanfeiwuji.isupabase.request.filter.Filter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.ArrayList;
import java.util.List;

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

    public void handlerHorizontalFilter(MultiValueMap<String, String> params,
                                        TableInfo tableInfo,
                                        QueryWrapper queryWrapper) {
        List<QueryCondition> queryConditions = new ArrayList<>();
        params.forEach((key, value) ->
                queryConditions
                        .addAll(value.stream()
                                .map(it -> new Filter(key, it, tableInfo))
                                .map(Filter::toQueryCondition).toList())
        );
        QueryCondition queryCondition = queryConditions.stream().reduce(QueryCondition::and).orElse(QueryCondition.createEmpty());
        queryWrapper.and(queryCondition);
    }

}
