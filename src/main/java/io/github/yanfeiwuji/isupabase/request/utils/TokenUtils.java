package io.github.yanfeiwuji.isupabase.request.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.SpringApplication;

import io.github.yanfeiwuji.isupabase.IsupabaseApplication;

@UtilityClass
public class TokenUtils {

    public Pattern opDot(String op) {
        return Pattern.compile("^%s\\.(.*)".formatted(op));
    }

    public Pattern quantOp(String op) {
        return Pattern.compile("^%s(\\((any|all)\\))?\\.(.*)".formatted(op));
    }

    public static void main(String[] args) {
        Pattern pattern = TokenUtils.opDot("eq");
        Matcher matcher = pattern.matcher("eq.1");
        matcher.find();
        matcher.group(0);
        System.out.println(matcher.group(matcher.groupCount()));

    }

}
