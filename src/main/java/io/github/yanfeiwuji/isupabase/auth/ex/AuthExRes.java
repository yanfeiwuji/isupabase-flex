package io.github.yanfeiwuji.isupabase.auth.ex;

/**
 * @author yanfeiwuji
 * @date 2024/6/10 10:39
 */
public record AuthExRes(String error, String errorDescription) {
    public static AuthExRes INVALID_GRANT = new AuthExRes("invalid_grant", "Invalid login credentials");
}
