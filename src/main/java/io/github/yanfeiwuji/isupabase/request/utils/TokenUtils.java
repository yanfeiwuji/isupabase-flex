package io.github.yanfeiwuji.isupabase.request.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class TokenUtils {


    public Pattern opDot(String op) {
        return Pattern.compile("^%s\\.(.*)".formatted(op));
    }

    public Pattern quantOp(String op) {
        return Pattern.compile("^%s(\\((any|all)\\))?\\.(.*)".formatted(op));
    }

}
