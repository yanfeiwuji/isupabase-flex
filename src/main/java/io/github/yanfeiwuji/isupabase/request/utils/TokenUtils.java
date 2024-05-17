package io.github.yanfeiwuji.isupabase.request.utils;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class TokenUtils {

    public Pattern opDot(String op) {
        return Pattern.compile("^%s\\.(.*)".formatted(op));
    }

    public Pattern quantOp(String op) {
        return Pattern.compile("^%s(\\((any|all)\\))?\\.(.*)".formatted(op));
    }

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
