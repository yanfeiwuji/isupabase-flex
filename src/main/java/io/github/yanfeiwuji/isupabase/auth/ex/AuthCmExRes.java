package io.github.yanfeiwuji.isupabase.auth.ex;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 11:50
 */
public record AuthCmExRes(String code, String message,
                          @JsonAnyGetter Map<String, Object> ext) {
    public AuthCmExRes(String code, String message) {
        this(code, message, Map.of());
    }

    public static final AuthCmExRes EMAIL_EXISTS = new AuthCmExRes("email_exists", "A user with this email address has already been registered");

    public static final AuthCmExRes VALIDATION_FAILED_PASSWORD = new AuthCmExRes("validation_failed", "Signup requires a valid password");

    public static final AuthCmExRes VALIDATION_FAILED_EMAIL = new AuthCmExRes("validation_failed", "Unable to validate email address: invalid format");

    public static final AuthCmExRes PHONE_PROVIDER_DISABLE = new AuthCmExRes("phone_provider_disabled", "Phone signups are disabled");

    public static final AuthCmExRes SAME_PASSWORD = new AuthCmExRes("same_password", "New password should be different from the old password.");

    public AuthCmEx authCmEx() {
        return new AuthCmEx(this);
    }
}
