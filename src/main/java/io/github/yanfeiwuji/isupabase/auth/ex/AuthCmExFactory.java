package io.github.yanfeiwuji.isupabase.auth.ex;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.yanfeiwuji.isupabase.auth.utils.ISupabasePropertiesUtils;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 11:52
 */
@UtilityClass
public class AuthCmExFactory {

    public static final AuthCmEx VALIDATION_FAILED_PASSWORD = AuthCmExRes.VALIDATION_FAILED_PASSWORD.authCmEx();
    public static final AuthCmEx VALIDATION_FAILED_EMAIL = AuthCmExRes.VALIDATION_FAILED_EMAIL.authCmEx();

    public static final AuthCmEx PHONE_PROVIDER_DISABLE = AuthCmExRes.PHONE_PROVIDER_DISABLE.authCmEx();
    public static final AuthCmEx SAME_PASSWORD = AuthCmExRes.SAME_PASSWORD.authCmEx();
    public static final AuthCmEx EMAIL_EXISTS = AuthCmExRes.EMAIL_EXISTS.authCmEx();
    private static final AuthCmEx UNSUPPORTED_PROVIDER = AuthCmExRes.UNSUPPORTED_PROVIDER.authCmEx();

    public AuthCmEx weakPassword(List<String> reasons) {
        final Map<String, String> stringStringMap = ISupabasePropertiesUtils.weakPasswordMap();
        final String message = reasons.stream().map(stringStringMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(CharSequenceUtil.SPACE));

        final AuthCmExRes authCmExRes = new AuthCmExRes("weak_password", message, Map.of("weak_password", Map.of("reasons", reasons)));
        return new AuthCmEx(authCmExRes);
    }

    public AuthCmEx validatePassword() {
        return VALIDATION_FAILED_PASSWORD;
    }

    public AuthCmEx validateEmail() {
        return VALIDATION_FAILED_EMAIL;
    }

    public AuthCmEx unsupportedProvider() {
        return UNSUPPORTED_PROVIDER;
    }
}
