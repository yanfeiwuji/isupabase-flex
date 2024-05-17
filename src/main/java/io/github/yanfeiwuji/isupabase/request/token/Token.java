package io.github.yanfeiwuji.isupabase.request.token;

import io.github.yanfeiwuji.isupabase.request.filter.IToken;

import java.util.regex.Pattern;

public record Token(String mark, Pattern pattern) implements IToken {

}
