package io.github.yanfeiwuji.isupabase.request.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenModifiers implements IToken {

    ALL("(all)"), ANY("(any)"), NONE("");

    private String mark;
}
