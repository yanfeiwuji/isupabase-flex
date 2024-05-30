package io.github.yanfeiwuji.isupabase.request;

import io.github.yanfeiwuji.isupabase.request.ex.PgrstEx;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;


public interface IReqHandler {

    String PATH_PARAM = "tableName";
    String ROUTE_PATH = String.format("{%s}", PATH_PARAM);
    String REQ_TABLE_INFO_KEY = "reqTableInfo";
    String REQ_API_REQ_KEY = "reqApiReq";

    String REQ_TABLE_MAPPER_KEY = "reqTableMapper";

    ServerRequest before(ServerRequest request);

    ServerResponse get(ServerRequest request) throws Exception;

    ServerResponse post(ServerRequest request) throws Exception;

    ServerResponse put(ServerRequest request) throws Exception;

    ServerResponse patch(ServerRequest request) throws Exception;

    ServerResponse delete(ServerRequest request) throws Exception;

    ServerResponse after(ServerRequest request, ServerResponse response);

    ServerResponse onError(Throwable throwable, ServerRequest request);

    default RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions
                .route()
                .path(ROUTE_PATH,
                        builder -> builder.before(this::before)
                                .GET(this::get)
                                .POST(this::post)
                                .PUT(this::put)
                                .PATCH(this::patch)
                                .DELETE(this::delete)
                                .after(this::after)
                )
                .onError(PgrstEx.class, this::onError)
                .build();

    }
}
