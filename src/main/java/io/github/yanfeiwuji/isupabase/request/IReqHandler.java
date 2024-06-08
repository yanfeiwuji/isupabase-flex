package io.github.yanfeiwuji.isupabase.request;

import io.github.yanfeiwuji.isupabase.request.ex.PgrstEx;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;


public interface IReqHandler {

    String PATH_PARAM = "tableName";
    String ROUTE_PATH = String.format("/rest/v1/{%s}", PATH_PARAM);

    String REQ_API_REQ_KEY = "reqApiReq";

    ServerRequest before(ServerRequest request);

    ServerResponse handler(ServerRequest request) throws Exception;

    ServerResponse after(ServerRequest request, ServerResponse response);

    ServerResponse onError(Throwable throwable, ServerRequest request);

    default RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions
                .route()
                .path(ROUTE_PATH,
                        builder -> builder.before(this::before)
                                .GET(this::handler)
                                .POST(this::handler)
                                // .PUT(this::put)
                                .PATCH(this::handler)
                                .DELETE(this::handler)
                                .after(this::after)
                )
                .onError(PgrstEx.class, this::onError)
                .build();

    }
}
