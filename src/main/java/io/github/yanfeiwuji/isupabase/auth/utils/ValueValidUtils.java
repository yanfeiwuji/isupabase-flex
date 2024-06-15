package io.github.yanfeiwuji.isupabase.auth.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * @author yanfeiwuji
 * @date 2024/6/14 13:37
 */
@UtilityClass
public class ValueValidUtils {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public static boolean isEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

}
