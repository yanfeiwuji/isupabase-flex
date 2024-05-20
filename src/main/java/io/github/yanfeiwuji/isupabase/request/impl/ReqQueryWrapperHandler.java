package io.github.yanfeiwuji.isupabase.request.impl;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;

import io.github.yanfeiwuji.isupabase.request.IReqQueryWrapperHandler;
import io.github.yanfeiwuji.isupabase.request.filter.Filter;
import io.github.yanfeiwuji.isupabase.request.req.ApiReq;
import io.github.yanfeiwuji.isupabase.request.select.Select;
import io.github.yanfeiwuji.isupabase.request.utils.ParamKeyUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.*;

@Component
@AllArgsConstructor
public class ReqQueryWrapperHandler implements IReqQueryWrapperHandler {

    @Override
    public void handler(ServerRequest request, TableInfo tableInfo, QueryChain<?> queryChain) {
        MultiValueMap<String, String> params = request.params();
        Select select = handlerSelect(params, tableInfo);
        List<Filter> filters = handlerHorizontalFilter(params, tableInfo);
        new ApiReq(select, filters, List.of()).handler(queryChain);
    }

    public Select handlerSelect(MultiValueMap<String, String> params,
            TableInfo tableInfo) {
        String selectValue = Optional.ofNullable(params.getFirst(ParamKeyUtils.SELECT_KEY))
                .orElse("*");
        return new Select(selectValue, tableInfo, null);

    }

    public List<Filter> handlerHorizontalFilter(MultiValueMap<String, String> params,
            TableInfo tableInfo) {
        return params.entrySet().stream()
                .filter(it -> ParamKeyUtils.canFilter(it.getKey()))
                .flatMap(kv -> kv.getValue().stream().map(v -> new Filter(kv.getKey(), v, tableInfo)))
                .toList();
        // .map(Filter::toQueryCondition)
        // .reduce(QueryCondition::and).orElse(QueryCondition.createEmpty());
    }

}
