package io.github.yanfeiwuji.isupabase.request.token;

import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

    public final Token WITH_SUB_KEY = new Token("with_sub_key", Pattern.compile("^([a-zA-Z1-9_.]*)\\.([a-zA-Z1-9_]*)"));

    public final Token INNER_LOGIC = new Token("inner_logic", Pattern.compile("^(and|or|not\\.and|not\\.or)\\((.*)\\)"));

    public final Token KEY_DOT_VALUE = new Token("key_dot_value", Pattern.compile("^([a-zA-Z1-9_]*)\\.(.*)"));

    public final Token OP_VALUE = new Token("op_value", Pattern.compile("^(?:not.)?([a-zA-Z1-9_]*)\\.(.*)"));

    public final Token IN_VALUE = new Token("in_value",
            Pattern.compile("\\((?:\"[^\"]*\"|[^,)]+)\\)(?:,\s*(?:\"[^\"]*\"|[^,)]+))*")
    );

    public static void main(String[] args) {

    }


}
