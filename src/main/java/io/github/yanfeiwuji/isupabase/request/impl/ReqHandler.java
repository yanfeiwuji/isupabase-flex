package io.github.yanfeiwuji.isupabase.request.impl;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.mybatis.Mappers;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import io.github.yanfeiwuji.isupabase.request.BodyInfo;
import io.github.yanfeiwuji.isupabase.request.IBodyHandler;
import io.github.yanfeiwuji.isupabase.request.IReqHandler;
import io.github.yanfeiwuji.isupabase.request.IReqQueryWrapperHandler;
import io.github.yanfeiwuji.isupabase.request.ex.DbExManagers;
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
    private final IBodyHandler bodyHandler;

    @Override
    public ServerRequest before(ServerRequest request) {
        String tableName = request.pathVariable(PATH_PARAM);

        TableInfo tableInfo = Optional.of(request.pathVariable(PATH_PARAM))
                .map(TableInfoFactory::ofTableName)
                .orElseThrow(DbExManagers.UNDEFINED_TABLE.supplierReqEx(tableName));

        request.servletRequest().setAttribute(
                REQ_TABLE_INFO_KEY,
                tableInfo
        );
        request.servletRequest().setAttribute(
                REQ_QUERY_WRAPPER_KEY,
                reqQueryWrapperHandler.handler(request, tableInfo)
        );
        request.servletRequest().setAttribute(
                REQ_TABLE_MAPPER_KEY,
                Mappers.ofEntityClass(tableInfo.getEntityClass())
        );

        return request;
    }

    @Override
    public ServerResponse get(ServerRequest request) {
        return ServerResponse.ok().body(mapper(request).selectListByQuery(queryWrapper(request)));
    }

    @Override
    public ServerResponse post(ServerRequest request) {

        TableInfo tableInfo = tableInfo(request);
        BodyInfo<?> bodyInfo = bodyHandler.handler(request, tableInfo.getEntityClass());

        Optional.ofNullable(bodyInfo)
                .map(BodyInfo::getSingle)
                .ifPresent(it -> mapper(request).insert(it));
        Optional.ofNullable(bodyInfo)
                .map(BodyInfo::getArray)
                .map(it -> (List<Object>) it)
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

        return Optional.ofNullable(bodyInfo).map(BodyInfo::getSingle)
                .map(it ->
                        {
                            mapper(request).updateByQuery(it, queryWrapper(request));
                            return it;
                        }
                ).map(it -> ServerResponse.ok().body(it))
                .orElse(ServerResponse.ok().build());
    }

    @Override
    public ServerResponse delete(ServerRequest request) {
        QueryWrapper wrapper = queryWrapper(request);
        mapper(request).deleteByQuery(wrapper);
        return ServerResponse.ok().build();
    }

    @Override
    public ServerResponse after(ServerRequest request, ServerResponse response) {
        return response;
    }

    @Override
    public ServerResponse onError(Throwable throwable, ServerRequest request) {
        log.info("ERROR HERE");
        return Optional.of(throwable)
                .filter(ReqEx.class::isInstance)
                .map(ReqEx.class::cast)
                .map(ReqEx::toResponse)
                .orElse(ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    private TableInfo tableInfo(ServerRequest request) {
        return request
                .attribute(REQ_TABLE_INFO_KEY).map(TableInfo.class::cast)
                .orElseThrow();
    }

    private QueryWrapper queryWrapper(ServerRequest request) {
        return request
                .attribute(REQ_QUERY_WRAPPER_KEY).map(QueryWrapper.class::cast)
                .orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private <T> BaseMapper<T> mapper(ServerRequest request) {
        return request
                .attribute(REQ_TABLE_MAPPER_KEY).map(it -> (BaseMapper<T>) it)
                .orElseThrow();
    }
}
