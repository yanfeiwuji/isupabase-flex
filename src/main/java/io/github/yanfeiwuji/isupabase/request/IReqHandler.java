package io.github.yanfeiwuji.isupabase.request;

import io.github.yanfeiwuji.isupabase.request.ex.ReqEx;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

public interface IReqHandler {

    String PATH_PARAM = "tableName";
    String ROUTE_PATH = String.format("{%s}", PATH_PARAM);
    String REQ_TABLE_INFO_KEY = "reqTableInfo";
    String REQ_QUERY_WRAPPER_KEY = "reqQueryWrapper";

    ServerRequest before(ServerRequest request);

    ServerResponse get(ServerRequest request) throws Exception;

    ServerResponse post(ServerRequest request) throws Exception;

    ServerResponse put(ServerRequest request) throws Exception;

    ServerResponse patch(ServerRequest request) throws Exception;

    ServerResponse delete(ServerRequest request) throws Exception;

    ServerResponse after(ServerRequest request, ServerResponse response);

    ServerResponse onError(Throwable throwable, ServerRequest request);

    default void route(RouterFunctions.Builder builder) {
        builder.before(this::before)
                .GET(this::get)
                .POST(this::post)
                .PUT(this::put)
                .PATCH(this::patch)
                .DELETE(this::delete)
                .after(this::after)
                .onError(ReqEx.class, this::onError);
    }
}
