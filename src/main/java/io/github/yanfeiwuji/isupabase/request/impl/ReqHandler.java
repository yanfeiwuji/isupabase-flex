package io.github.yanfeiwuji.isupabase.request.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.mybatis.Mappers;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import io.github.yanfeiwuji.isupabase.request.BodyInfo;
import io.github.yanfeiwuji.isupabase.request.IBodyHandler;
import io.github.yanfeiwuji.isupabase.request.IReqHandler;
import io.github.yanfeiwuji.isupabase.request.IReqQueryWrapperHandler;
import io.github.yanfeiwuji.isupabase.request.ex.*;
import io.github.yanfeiwuji.isupabase.request.req.ApiReq;
import io.github.yanfeiwuji.isupabase.request.select.ResultMapping;
import io.github.yanfeiwuji.isupabase.request.select.ResultMappingFactory;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class ReqHandler implements IReqHandler {
    private static final Logger log = LoggerFactory.getLogger(ReqHandler.class);
    private final IReqQueryWrapperHandler reqQueryWrapperHandler;
    private final IBodyHandler bodyHandler;
    private final ObjectMapper objectMapper;

    @Override
    public ServerRequest before(ServerRequest request) {
        String tableName = request.pathVariable(PATH_PARAM);

        TableInfo tableInfo = Optional.of(request.pathVariable(PATH_PARAM))
                .map(TableInfoFactory::ofTableName)
                .orElseThrow(PgrstExFactory.exTableNotFound(tableName));

        BaseMapper<?> baseMapper = Mappers.ofEntityClass(tableInfo.getEntityClass());

        ApiReq apiReq = reqQueryWrapperHandler.handler(request, tableName);

        request.servletRequest().setAttribute(REQ_TABLE_INFO_KEY, tableInfo);
        request.servletRequest().setAttribute(REQ_TABLE_MAPPER_KEY, baseMapper);
        request.servletRequest().setAttribute(REQ_API_REQ_KEY, apiReq);
        return request;
    }

    @Override
    public ServerResponse get(ServerRequest request) {
        ApiReq apiReq = apiReq(request);
        BaseMapper<?> baseMapper = mapper(request);


        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(apiReq.result(baseMapper, objectMapper));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ServerResponse post(ServerRequest request) {
        TableInfo tableInfo = tableInfo(request);
        BodyInfo<?> bodyInfo = bodyHandler.handler(request, tableInfo.getEntityClass());

        Optional.ofNullable(bodyInfo)
                .map(BodyInfo::getSingle)
                .ifPresent(it -> mapper(request).insert(it));
        Optional.ofNullable(bodyInfo)
                .map(BodyInfo::getArray)
                .map(it -> (List) it)
                .ifPresent(it -> mapper(request).insertBatch(it));

        return ServerResponse.ok().build();
    }

    @Override
    public ServerResponse put(ServerRequest request) {
        TableInfo tableInfo = tableInfo(request);
        BodyInfo<?> bodyInfo = bodyHandler.handler(request, tableInfo.getEntityClass());

        Optional.ofNullable(bodyInfo).map(BodyInfo::getSingle)
                .ifPresent(it -> mapper(request).insertOrUpdate(it));
        return ServerResponse.ok().build();
    }

    @Override
    public ServerResponse patch(ServerRequest request) {
        TableInfo tableInfo = tableInfo(request);
        BodyInfo<?> bodyInfo = bodyHandler.handler(request, tableInfo.getEntityClass());
        ApiReq apiReq = apiReq(request);

        return Optional.ofNullable(bodyInfo).map(BodyInfo::getSingle)
                .map(it -> {
                    mapper(request).updateByQuery(it, apiReq.queryWrapper());
                    return it;
                }).map(it -> ServerResponse.ok().body(it))
                .orElse(ServerResponse.ok().build());
    }

    @Override
    public ServerResponse delete(ServerRequest request) {
        ApiReq apiReq = apiReq(request);
        mapper(request).deleteByQuery(apiReq.queryWrapper());
        return ServerResponse.ok().build();
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

    private TableInfo tableInfo(ServerRequest request) {
        return request
                .attribute(REQ_TABLE_INFO_KEY)
                .map(TableInfo.class::cast)
                .orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private <T> BaseMapper<T> mapper(ServerRequest request) {
        return request
                .attribute(REQ_TABLE_MAPPER_KEY).map(BaseMapper.class::cast)
                .orElseThrow();
    }

    private ApiReq apiReq(ServerRequest request) {
        return request
                .attribute(REQ_API_REQ_KEY).map(ApiReq.class::cast)
                .orElseThrow();
    }
}
