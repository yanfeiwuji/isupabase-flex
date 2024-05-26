package io.github.yanfeiwuji.isupabase.request.token;

import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.regex.Pattern;

@UtilityClass
public final class MTokens {
    public final Token DOT = new Token("dot", Pattern.compile("(.+?)\\.(.*)"));
    public final Token NOT = new Token("not", TokenUtils.opDot("not"));
    public final Token LOGIC_KV = new Token(
            "logic_kv", Pattern.compile("(and|or|not\\.and|not\\.or)(.*)"));

    public final Token SELECT_WITH_SUB = new Token("select_with_sub",
            Pattern.compile("^(.*?)\\((.*)\\)$"));

    public final Token ORDER_BY = new Token("order_by",
            Pattern.compile("([a-zA-Z1-9_]*)(?:\\.(asc|desc))?(?:\\.(nullsfirst|nullslast))?"));

    public final Token TOP_ORDER_BY = new Token("top_order_by",
            Pattern.compile("([a-zA-Z1-9_.]*)\\(([a-zA-Z1-9_]*)\\)(?:\\.(asc|desc))?(?:\\.(nullsfirst|nullslast))?"));

}
