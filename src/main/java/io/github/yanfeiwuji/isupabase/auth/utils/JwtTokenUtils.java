package io.github.yanfeiwuji.isupabase.auth.utils;

import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.vo.TokenInfo;

import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import lombok.experimental.UtilityClass;

/**
 * @author yanfeiwuji
 * @date 2024/6/16 09:12
 */
@UtilityClass
public class JwtTokenUtils {
    public String recoveryTokenToRedirectTo(TokenInfo<User> tokenInfo, String redirectTo) {
        return AuthStrPool.RECOVERY_ACCESS_TOKEN_URL_TEMP.formatted(
                redirectTo,
                tokenInfo.getAccessToken(),
                tokenInfo.getExpiresAt(),
                tokenInfo.getExpiresIn(),
                tokenInfo.getRefreshToken()
        );
    }

    public String emailChangeTokenToRedirectTo(TokenInfo<User> tokenInfo, String redirectTo) {
        return AuthStrPool.EMAIL_CHANGE_ACCESS_TOKEN_URL_TEMP.formatted(
                redirectTo,
                tokenInfo.getAccessToken(),
                tokenInfo.getExpiresAt(),
                tokenInfo.getExpiresIn(),
                tokenInfo.getRefreshToken()
        );
    }

    public String oauthTokenToRedirectTo(TokenInfo<User> tokenInfo, String redirectTo, String providerRefreshToken, String providerToken) {
        return AuthStrPool.OAUTH_ACCESS_TOKEN_URL_TEMP.formatted(
                redirectTo,
                tokenInfo.getAccessToken(),
                tokenInfo.getExpiresAt(),
                tokenInfo.getExpiresIn(),
                providerRefreshToken,
                providerToken,
                tokenInfo.getRefreshToken()
        );
    }

}
