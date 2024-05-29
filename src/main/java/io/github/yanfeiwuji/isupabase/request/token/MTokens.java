package io.github.yanfeiwuji.isupabase.request.token;

import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@UtilityClass
public final class MTokens {

    public final Token SELECT_WITH_SUB = new Token("select_with_sub",
            Pattern.compile("^(.*?)\\((.*)\\)$"));

//    public final Token ORDER_BY = new Token("order_by",
//            Pattern.compile("^([a-zA-Z1-9_]*)(?:\\.(asc|desc))?(?:\\.(nullsfirst|nullslast))?"));

    public final Token ORDER_BY = new Token("top_order_by",
            Pattern.compile("^([a-zA-Z1-9_]*)(?:\\(([a-zA-Z1-9_]*)\\))?(?:\\.(asc|desc))?(?:\\.(nullsfirst|nullslast))?$"));

    public final Token WITH_SUB_KEY = new Token("with_sub_key", Pattern.compile("^([a-zA-Z1-9_.]*)\\.([a-zA-Z1-9_]*)"));

    public final Token INNER_LOGIC = new Token("inner_logic", Pattern.compile("^(and|or|not\\.and|not\\.or)\\((.*)\\)"));

    public final Token KEY_DOT_VALUE = new Token("key_dot_value", Pattern.compile("^([a-zA-Z1-9_]*)\\.(.*)"));

    public final Token OP_VALUE = new Token("op_value", Pattern.compile("^(?:not.)?([a-zA-Z1-9_]*(?:\\(any\\)|\\(all\\))?)\\.(.*)"));

    public static void main(String[] args) {
        boolean b = ORDER_BY.find("dds5s(ds%");
        ORDER_BY.groups("dds5s(ds%").forEach(System.out::println);
        System.out.println(b);
    }


}
