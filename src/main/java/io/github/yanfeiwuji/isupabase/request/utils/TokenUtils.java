package io.github.yanfeiwuji.isupabase.request.utils;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class TokenUtils {

    public String removeRoundBrackets(String input) {
        return CharSequenceUtil.strip(input, "(", ")");
    }

    public String removeDelim(String input) {
        return CharSequenceUtil.strip(input, StrPool.DELIM_START, StrPool.DELIM_END);
    }

    public List<String> splitByCommaQuoted(String input) {
        List<String> result = new ArrayList<>();
        int start = 0;
        boolean inQuoted = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == CharPool.COMMA && !inQuoted) {
                result.add(
                        CharSequenceUtil.strip(input.substring(start, i).trim(), "\"", "\""));
                start = i + 1;
            } else if (c == CharPool.DOUBLE_QUOTES && !inQuoted) {
                inQuoted = true;
            } else if (c == CharPool.DOUBLE_QUOTES) {
                inQuoted = false;
            }
        }
        result.add(input.substring(start).trim());
        return result;
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
        final String endStr = input.substring(start).trim();
        if (CharSequenceUtil.isNotEmpty(endStr)) {
            result.add(input.substring(start).trim());
        }
        return result;
    }

}
