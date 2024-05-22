package io.github.yanfeiwuji.isupabase.request.impl;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;

import io.github.yanfeiwuji.isupabase.request.IReqQueryWrapperHandler;
import io.github.yanfeiwuji.isupabase.request.req.ApiReq;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;

@Component
@AllArgsConstructor
public class ReqQueryWrapperHandler implements IReqQueryWrapperHandler {

    @Override
    public ApiReq handler(ServerRequest request, String tableName) {

        return new ApiReq(request, tableName);
    }

}
