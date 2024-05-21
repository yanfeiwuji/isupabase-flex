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
        // Select select = handlerSelect(params, tableInfo);
        // List<Filter> filters = handlerHorizontalFilter(params, tableInfo);
        new ApiReq(request, tableInfo, queryChain);
    }

}
