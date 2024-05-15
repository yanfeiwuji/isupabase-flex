package io.github.yanfeiwuji.isupabase.request.filter;

import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public final class MTokens {
    final Token DOT = new Token("dot", Pattern.compile("(.*?)\\.(.*)"));
    final Token NOT = new Token("not", TokenUtils.opDot("not"));

}
