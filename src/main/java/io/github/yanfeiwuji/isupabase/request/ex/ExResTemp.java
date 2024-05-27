package io.github.yanfeiwuji.isupabase.request.ex;

import org.springframework.http.HttpStatus;

public record ExResTemp(String code, String detailsTemp, String hint, String messageTemp, HttpStatus httpStatus) {

    public ExRes toExRes(Object... args) {
        return new ExRes(code, detailsTemp, hint, messageTemp.formatted(args));
    }

    public ExRes toExResWithDetails(Object... args) {
        return new ExRes(
                code,
                detailsTemp.formatted(args),
                hint,
                messageTemp.formatted(args));
    }

}
