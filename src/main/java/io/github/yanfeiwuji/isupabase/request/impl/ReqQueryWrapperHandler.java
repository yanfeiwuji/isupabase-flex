package io.github.yanfeiwuji.isupabase.request.impl;

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
