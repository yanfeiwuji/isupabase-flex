package io.github.yanfeiwuji.isupabase.request.token;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.regex.Pattern;

@UtilityClass
public final class MTokens {

    public final Token SELECT_WITH_SUB = new Token("select_with_sub",
            Pattern.compile("^(?:[a-zA-Z1-9_]*:)?([a-zA-Z1-9_]*(?:!inner)?)\\((.*)\\)(?:::[a-zA-Z]*)?$"));

    // public final Token ORDER_BY = new Token("order_by",
    // Pattern.compile("^([a-zA-Z1-9_]*)(?:\\.(asc|desc))?(?:\\.(nullsfirst|nullslast))?"));
    public final Token SELECT_ITEM = new Token("select_item", Pattern.compile("^(?:[a-zA-Z1-9_]*:)?([a-zA-Z1-9_]*)(?:::[a-zA-Z]*)?$"));
    public final Token RENAME = new Token("rename", Pattern.compile("^([a-zA-Z1-9_]*):[a-zA-Z1-9(),]*"));
    public final Token CAST = new Token("cast", Pattern.compile("^.*?::([a-zA-Z1]*)$"));
    public final Token ORDER_BY = new Token("top_order_by",
            Pattern.compile("^([a-zA-Z1-9_]*)(?:\\(([a-zA-Z1-9_]*)\\))?(?:\\.(asc|desc))?(?:\\.(nullsfirst|nullslast))?$"));

    public final Token WITH_SUB_KEY = new Token("with_sub_key", Pattern.compile("^([a-zA-Z1-9_.]*)\\.([a-zA-Z1-9_]*)"));

    public final Token INNER_LOGIC = new Token("inner_logic",
            Pattern.compile("^(and|or|not\\.and|not\\.or)\\((.*)\\)"));

    public final Token KEY_DOT_VALUE = new Token("key_dot_value", Pattern.compile("^([a-zA-Z1-9_]*)\\.(.*)"));

    public final Token OP_VALUE = new Token("op_value",
            Pattern.compile("^(?:not.)?([a-zA-Z1-9_]*(?:\\(any\\)|\\(all\\))?)\\.(.*)"));

    public static void main(String[] args) {
        // CAST.first("as::text").ifPresent(System.out::println);
    }
}
