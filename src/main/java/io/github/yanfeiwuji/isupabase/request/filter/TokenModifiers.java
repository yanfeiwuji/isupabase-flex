package io.github.yanfeiwuji.isupabase.request.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenModifiers implements IToken {
    NONE(""),
    ALL("{all}"), ANY("{any}");
    private String mark;
}
