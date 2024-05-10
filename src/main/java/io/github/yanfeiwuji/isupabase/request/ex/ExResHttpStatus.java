package io.github.yanfeiwuji.isupabase.request.ex;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.ServerResponse;

public record ExResHttpStatus(ExRes exRes, HttpStatus httpStatus) {
    public ServerResponse toResponse() {
        return ServerResponse.status(httpStatus)
                .body(exRes);
    }

    public ReqEx toEx() {
        return new ReqEx(this);
    }
}
