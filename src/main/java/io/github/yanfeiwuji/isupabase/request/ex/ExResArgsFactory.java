package io.github.yanfeiwuji.isupabase.request.ex;

import java.util.List;
import java.util.stream.Stream;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExResArgsFactory {

    public ExResArgs ofMessageArgs(String... args) {
        return new ExResArgs(List.of(), List.of(), Stream.of(args).toList());
    }

    public ExResArgs ofMsgArgs(String... args) {
        return new ExResArgs(List.of(), List.of(), Stream.of(args).toList());
    }

}
