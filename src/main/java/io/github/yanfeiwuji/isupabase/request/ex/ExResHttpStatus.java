package io.github.yanfeiwuji.isupabase.request.ex;

import org.springframework.web.servlet.function.ServerResponse;

public record ExResHttpStatus(ExRes exRes, Integer httpStatus) {
    public ServerResponse toResponse() {
        return ServerResponse.status(httpStatus)
                .body(exRes);
    }
}
