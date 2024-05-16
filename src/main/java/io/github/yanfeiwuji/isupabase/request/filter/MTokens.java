package io.github.yanfeiwuji.isupabase.request.filter;

import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@UtilityClass
public final class MTokens {
    final Token DOT = new Token("dot", Pattern.compile("(.*?)\\.(.*)"));
    final Token NOT = new Token("not", TokenUtils.opDot("not"));
    final Token LOGIC_KV = new Token(
            "logic_kv", Pattern.compile("(and|or|not\\.and|not\\.or)(.*)"));

    // final Token COMMA_SEPARATOR = new Token("comma_separator",
    // Pattern.compile(",(?=(?:[^()]|(?R))*\\))"));

    public List<String> splitByComma(String input) {
        List<String> result = new ArrayList<>();
        int start = 0;
        int bracketLevel = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '(') {
                bracketLevel++;
            } else if (c == ')') {
                bracketLevel--;
            } else if (c == ',' && bracketLevel == 0) {
                result.add(input.substring(start, i).trim());
                start = i + 1;
            }
        }
        result.add(input.substring(start).trim());
        return result;
    }

}
