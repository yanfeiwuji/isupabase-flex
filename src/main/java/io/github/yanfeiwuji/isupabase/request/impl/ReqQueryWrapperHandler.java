package io.github.yanfeiwuji.isupabase.request.impl;

import cn.hutool.core.text.StrPool;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;

import io.github.yanfeiwuji.isupabase.constants.ParamKey;
import io.github.yanfeiwuji.isupabase.request.IReqQueryWrapperHandler;
import io.github.yanfeiwuji.isupabase.request.filter.Filter;
import io.github.yanfeiwuji.isupabase.request.select.Select;
import io.github.yanfeiwuji.isupabase.request.utils.ParamKeyUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class ReqQueryWrapperHandler implements IReqQueryWrapperHandler {


    @Override
    public QueryWrapper handler(ServerRequest request, TableInfo tableInfo) {
        MultiValueMap<String, String> params = request.params();
        QueryWrapper wrapper = QueryWrapper.create();
        handlerSelect(params, tableInfo, wrapper);


        handlerHorizontalFilter(params, tableInfo, wrapper);
        return wrapper;
    }

    public void handlerSelect(MultiValueMap<String, String> params,
                              TableInfo tableInfo,
                              QueryWrapper queryWrapper) {
        String selectValue =
                Optional.ofNullable(params.getFirst(ParamKeyUtils.SELECT_KEY))
                        .orElse("*");

        new Select(selectValue, tableInfo).handlerQueryWrapper(queryWrapper);

    }

    public void handlerHorizontalFilter(MultiValueMap<String, String> params,
                                        TableInfo tableInfo,
                                        QueryWrapper queryWrapper) {

        params.entrySet().stream()
                .filter(it -> ParamKeyUtils.canFilter(it.getKey()))
                .flatMap(kv -> kv.getValue().stream().map(v -> new Filter(kv.getKey(), v, tableInfo)))
                .map(Filter::toQueryCondition)
                .reduce(QueryCondition::and)
                .ifPresent(queryWrapper::where);


    }

}
