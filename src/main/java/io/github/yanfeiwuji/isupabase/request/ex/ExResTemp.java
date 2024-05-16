package io.github.yanfeiwuji.isupabase.request.ex;

import org.springframework.http.HttpStatus;

public record ExResTemp(String code, String details, String hint, String messageTemp, HttpStatus httpStatus) {

    public ExRes toExRes(Object... args) {
        return new ExRes(code, details, hint, messageTemp.formatted(args));
    }

}
