package io.github.yanfeiwuji.isupabase.request.token;

import java.util.regex.Pattern;

public record Token(String mark, Pattern pattern) implements IToken {

}
