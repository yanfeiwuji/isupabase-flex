package io.github.yanfeiwuji.isupabase.request;

import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstEx;
import org.springframework.web.servlet.function.*;

import java.util.regex.Pattern;

import static org.springframework.web.servlet.function.RequestPredicates.*;


public interface IReqHandler {

    String PATH_PARAM = "tableName";
    String ROUTE_PATH = String.format("%s/{%s}", PgrstStrPool.REST_PATH, PATH_PARAM);
    Pattern PATH_PATTERN = Pattern.compile(PgrstStrPool.REST_PATH + "/.*[^rpc]");


    String REQ_API_REQ_KEY = "reqApiReq";

    ServerRequest before(ServerRequest request);

    ServerResponse handler(ServerRequest request) throws Exception;

    ServerResponse after(ServerRequest request, ServerResponse response);

    ServerResponse onError(Throwable throwable, ServerRequest request);


    default RouterFunction<ServerResponse> routerFunction() {

        return RouterFunctions
                .route()
                .before(this::before)
                .route(isValidPath().and(GET(ROUTE_PATH)), this::handler)
                .route(isValidPath().and(POST(ROUTE_PATH)), this::handler)
                .route(isValidPath().and(PATCH(ROUTE_PATH)), this::handler)
                .route(isValidPath().and(DELETE(ROUTE_PATH)), this::handler)
                .after(this::after)
                .onError(PgrstEx.class, this::onError)
                .build();

    }

    private RequestPredicate isValidPath() {
        return request -> {
            String path = request.uri().getPath();
            return PATH_PATTERN.matcher(path).find();
        };
    }
}
