package io.github.yanfeiwuji.isupabase.request.impl;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.mybatis.Mappers;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import io.github.yanfeiwuji.isupabase.request.IReqHandler;
import io.github.yanfeiwuji.isupabase.request.ex.*;
import io.github.yanfeiwuji.isupabase.request.req.ApiReq;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Optional;

@Component
@AllArgsConstructor
public class ReqHandler implements IReqHandler {

    @Override
    public ServerRequest before(ServerRequest request) {
        String tableName = request.pathVariable(PATH_PARAM);

        TableInfo tableInfo = Optional.of(request.pathVariable(PATH_PARAM))
                .map(TableInfoFactory::ofTableName)
                .orElseThrow(PgrstExFactory.exTableNotFound(tableName));

        BaseMapper<Object> baseMapper = (BaseMapper<Object>) Mappers.ofEntityClass(tableInfo.getEntityClass());
        ApiReq apiReq = new ApiReq(request, tableName, baseMapper);

        request.servletRequest().setAttribute(REQ_API_REQ_KEY, apiReq);
        return request;
    }

    @Override
    public ServerResponse get(ServerRequest request) {
        return handler(request);
    }

    @Override
    public ServerResponse post(ServerRequest request) {
        return handler(request);
    }

    @Override
    public ServerResponse put(ServerRequest request) {
        return handler(request);
    }

    @Override
    public ServerResponse patch(ServerRequest request) {
        return handler(request);
    }

    @Override
    public ServerResponse delete(ServerRequest request) {
        return handler(request);
    }

    private ServerResponse handler(ServerRequest request) {
        final ApiReq apiReq = apiReq(request);
        return apiReq.handler();
    }

    @Override
    public ServerResponse after(ServerRequest request, ServerResponse response) {
        return response;
    }

    @Override
    public ServerResponse onError(Throwable throwable, ServerRequest request) {
        return Optional.of(throwable)
                .filter(PgrstEx.class::isInstance)
                .map(PgrstEx.class::cast)
                .map(PgrstEx::toResponse)
                .orElse(ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    private ApiReq apiReq(ServerRequest request) {
        return request
                .attribute(REQ_API_REQ_KEY).map(ApiReq.class::cast)
                .orElseThrow();
    }
}
