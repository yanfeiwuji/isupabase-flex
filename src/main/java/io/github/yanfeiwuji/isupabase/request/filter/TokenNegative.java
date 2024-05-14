package io.github.yanfeiwuji.isupabase.request.filter;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenNegative implements IToken {
    NOT("not");

    private String mark;

    public static boolean strIsNegative(String str) {
        return str.startsWith(NOT.mark + StrUtil.DOT);
    }

    public static String removeNotDot(String str) {
        return StrUtil.replace(str, NOT.mark + StrUtil.DOT, "");
    }
}
