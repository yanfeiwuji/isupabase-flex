package io.github.yanfeiwuji.isupabase.auth.ex;

import lombok.experimental.UtilityClass;
import org.springframework.core.SpringProperties;

import java.util.List;
import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 10:48
 */
@UtilityClass
public class AuthExFactory {
    public static final AuthEx INVALID_GRANT_REFRESH_TOKEN_NOT_FOUND = AuthExRes.INVALID_GRANT_REFRESH_TOKEN_NOT_FOUND.authEx();
    public static final AuthEx INVALID_GRANT_ALREADY_USED = AuthExRes.INVALID_GRANT_ALREADY_USED.authEx();
    public static final AuthEx INVALID_GRANT_EMAIL_NOT_CONFIRMED = AuthExRes.INVALID_GRANT_EMAIL_NOT_CONFIRMED.authEx();
    public static final AuthEx NOT_IMPLEMENTED = AuthExRes.NOT_IMPLEMENTED.authEx();

}
