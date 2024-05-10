package io.github.yanfeiwuji.isupabase.request.impl;

import com.mybatisflex.core.mybatis.Mappers;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.request.IReqHandler;
import io.github.yanfeiwuji.isupabase.request.IReqQueryWrapperHandler;
import io.github.yanfeiwuji.isupabase.request.ex.ExResHttpStatus;
import io.github.yanfeiwuji.isupabase.request.ex.ReqEx;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@AllArgsConstructor
public class ReqHandler implements IReqHandler {
    private final IReqQueryWrapperHandler reqQueryWrapperHandler;

    @Override
    public ServerRequest before(ServerRequest request) {

        Optional.of(request.pathVariable(PATH_PARAM))
                .map(TableInfoFactory::ofTableName)
                // .orElseThrow(()->n)
                .ifPresent(it -> {
                            request.servletRequest().setAttribute(REQ_TABLE_INFO_KEY, it);
                            request.servletRequest().setAttribute(REQ_QUERY_WRAPPER_KEY, reqQueryWrapperHandler.handler(request, it));
                        }
                );
        return request;
    }

    @Override
    public ServerResponse get(ServerRequest request) {
        tableInfo(request);

        return ServerResponse.ok().body(List.of("1", "2", "3"));
    }

    @Override
    public ServerResponse post(ServerRequest request) {
        return ServerResponse.ok().build();
    }

    @Override
    public ServerResponse put(ServerRequest request) {
        return null;
    }

    @Override
    public ServerResponse patch(ServerRequest request) {
        return null;
    }

    @Override
    public ServerResponse delete(ServerRequest request) {
        return null;
    }

    @Override
    public ServerResponse after(ServerRequest request, ServerResponse response) {
        return response;
    }

    @Override
    public ServerResponse onError(Throwable throwable, ServerRequest request) {
        return Optional.of(throwable)
                .filter(ReqEx.class::isInstance)
                .map(ReqEx.class::cast)
                .map(ReqEx::getExResHttpStatus)
                .map(ExResHttpStatus::toResponse)
                .orElse(ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    private TableInfo tableInfo(ServerRequest request) {
        return request
                .attribute(REQ_TABLE_INFO_KEY).map(TableInfo.class::cast)
                .orElseThrow();
    }
}
