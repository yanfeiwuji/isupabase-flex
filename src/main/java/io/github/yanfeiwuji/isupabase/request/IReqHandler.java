package io.github.yanfeiwuji.isupabase.request;

import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstEx;
import org.springframework.web.servlet.function.*;

import static org.springframework.web.servlet.function.RouterFunctions.route;


public interface IReqHandler {

    String PATH_PARAM = "tableName";
    String ROUTE_PATH = PgrstStrPool.REST_PATH + "/{tableName:(?!rpc$)[a-zA-Z0-9_]+}";


    String REQ_API_REQ_KEY = "reqApiReq";

    ServerRequest before(ServerRequest request);

    ServerResponse handler(ServerRequest request);

    ServerResponse after(ServerRequest request, ServerResponse response);

    ServerResponse onError(Throwable throwable, ServerRequest request);


    default RouterFunction<ServerResponse> routerFunction() {

        return route()
                .path(IReqHandler.ROUTE_PATH, b -> b
                        .before(this::before)
                        .GET(this::handler)
                        .POST(this::handler)
                        .PATCH(this::handler)
                        .DELETE(this::handler)
                        .after(this::after)
                        .build()
                ).onError(PgrstEx.class, this::onError)
                .build();


    }


}
