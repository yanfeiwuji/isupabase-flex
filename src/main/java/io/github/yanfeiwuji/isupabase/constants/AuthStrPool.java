package io.github.yanfeiwuji.isupabase.constants;

/**
 * @author yanfeiwuji
 * @date 2024/6/10 17:07
 */
public class AuthStrPool {
    private AuthStrPool() {
    }

    public static final String JWT_ROLE_KEY = "role";
    public static final String ANON_ROLE = "anon";
    public static final String AUTHENTICATED_ROLE = "authenticated";

    public static final String AUTHENTICATED_AUD = "authenticated";

    public static final String USER_AGENT_KEY = "User-Agent";
    public static final String LOGOUT_SCOPE_GLOBAL = "global";
    public static final String LOGOUT_SCOPE_LOCAL = "local";
    public static final String LOGOUT_SCOPE_OTHERS = "others";

    public static final String TOKE_TYPE = "bearer";

    public static final String WEAK_PASSWORD_LENGTH = "length";
    public static final String WEAK_PASSWORD_CHARACTERS = "characters";


    public static final String ORIGIN_HEADER_KEY = "Origin";

    public static final String CONFIRM_SIGN_UP_URL_TEMP =
            "%s/" + PgrstStrPool.AUTH_RPC_PATH + "/verify?token=%s&type=signup&redirect_to=%s";

    public static final String CONFIRM_RECOVER_URL_TEMP =
            "%s/" + PgrstStrPool.AUTH_RPC_PATH + "/verify?token=%s&type=recovery&redirect_to=%s";

    public static final String ERROR_EMAIL_LINK_URL_TEMP = "%s/#error=access_denied&error_code=403&error_description=Email+link+is+invalid+or+has+expired";


    public static final String RECOVERY_ACCESS_TOKEN_URL_TEMP = "%s/#access_token=%s&expires_at=%d&expires_in=%d&refresh_token=%s&token_type=bearer&type=recovery";


    public static final String EMAIL_CONTENT_TYPE = "text/html; charset=utf-8";

    public static final String VERIFY_TYPE_SIGNUP = "signup";
    public static final String VERIFY_TYPE_RECOVERY = "recovery";

    public static final String QUERY_PARAM_ERROR = "error";
    public static final String QUERY_PARAM_ERROR_CODE = "error_code";
    public static final String QUERY_PARAM_ERROR_CODE_DEFAULT_VALUE = "403";
    public static final String QUERY_PARAM_ERROR_DESCRIPTION = "error_description";

    public static final String PASSWORD = "password";
    public static final String OTP = "otp";


}
