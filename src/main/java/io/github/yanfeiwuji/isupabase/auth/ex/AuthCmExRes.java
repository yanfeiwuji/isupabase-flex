package io.github.yanfeiwuji.isupabase.auth.ex;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.Map;
import java.util.Objects;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 11:50
 */
public record AuthCmExRes(String code, String message,

                          @JsonAnyGetter Map<String, Object> ext) {
    public static AuthCmExRes VALIDATION_FAILED_PASSWORD = new AuthCmExRes("validation_failed", "Signup requires a valid password", Map.of());

    public static AuthCmExRes VALIDATION_FAILED_EMAIL = new AuthCmExRes("validation_failed", "Unable to validate email address: invalid format", Map.of());

    public static AuthCmExRes PHONE_PROVIDER_DISABLE = new AuthCmExRes("phone_provider_disabled", "Phone signups are disabled", Map.of());

    public AuthCmEx authCmEx() {
        return new AuthCmEx(this);
    }
}
