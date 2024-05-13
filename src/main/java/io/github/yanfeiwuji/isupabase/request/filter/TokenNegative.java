package io.github.yanfeiwuji.isupabase.request.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenNegative implements IToken {
    NOT("not");

    private String mark;
}
