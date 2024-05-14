package io.github.yanfeiwuji.isupabase.request.filter;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenNegative implements IToken {
    NOT("not");

    private String mark;

    public static boolean paramKeyIsNegative(String paramKey) {
        return paramKey.startsWith(NOT.mark + StrUtil.DOT);
    }

    public static String removeNotDot(String paramKey) {
        return StrUtil.replace(paramKey, NOT.mark + StrUtil.DOT, "");
    }
}
