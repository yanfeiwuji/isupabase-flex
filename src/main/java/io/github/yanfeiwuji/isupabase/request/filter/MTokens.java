package io.github.yanfeiwuji.isupabase.request.filter;

import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public final class MTokens {
    final Token DOT = new Token("dot", Pattern.compile("(.*?)\\.(.*)"));
    final Token NOT = new Token("not", TokenUtils.opDot("not"));
    final Token LOGIC_KV = new Token(
            "logic_kv", Pattern.compile("(and|or|not\\.and|not\\.or)(.*)"));

    public static void main(String[] args) {
        boolean b = LOGIC_KV.find("not.and(asd)");
        LOGIC_KV.keyValue("not.and(asd)").ifPresent(System.out::println);
        System.out.println(b);
    }
}
