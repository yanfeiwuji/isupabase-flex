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


}
