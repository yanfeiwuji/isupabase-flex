package io.github.yanfeiwuji.isupabase.request.ex;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;

public record ExResTemp(String code, String detailsTemp, String hintTemp, String messageTemp, HttpStatus httpStatus) {

    public ExRes toExRes(Object... args) {
        return new ExRes(code,
                Optional.ofNullable(detailsTemp)
                        .map(it -> it.formatted(args)).orElse(null),
                Optional.ofNullable(hintTemp)
                        .map(it -> it.formatted(args)).orElse(null),
                Optional.ofNullable(messageTemp)
                        .map(it -> it.formatted(args)).orElse(null));
    }

    public ExRes toExRes(ExResArgs args) {
        return new ExRes(code,
                need(detailsTemp, args.detailsArgs()),
                need(hintTemp, args.hintArgs()),
                need(messageTemp, args.messageArgs()));
    }

    private String need(String temp, List<String> arg) {
        return Optional.ofNullable(temp)
                .map(it -> it.formatted(Optional.ofNullable(arg).orElse(List.of()).toArray()))
                .orElse(null);
    }

}
