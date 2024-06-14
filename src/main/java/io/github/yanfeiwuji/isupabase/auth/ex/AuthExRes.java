package io.github.yanfeiwuji.isupabase.auth.ex;

/**
 * @author yanfeiwuji
 * @date 2024/6/10 10:39
 */
public record AuthExRes(String error, String errorDescription) {
    public static AuthExRes INVALID_GRANT = new AuthExRes("invalid_grant", "Invalid login credentials");
    public static AuthExRes INVALID_GRANT_REFRESH_TOKEN_NOT_FOUND = new AuthExRes("invalid_grant", "Invalid Refresh Token: Refresh Token Not Found");
    public static AuthExRes INVALID_GRANT_ALREADY_USED = new AuthExRes("invalid_grant", "Invalid Refresh Token: Already Used");
    public static AuthExRes INVALID_GRANT_EMAIL_NOT_CONFIRMED = new AuthExRes("invalid_grant", "Email not confirmed");
    // email info
    public static AuthExRes EMAIL_LINK_ERROR = new AuthExRes("access_denied", "Email+link+is+invalid+or+has+expired");

    public AuthEx authEx() {
        return new AuthEx(this);
    }
}
