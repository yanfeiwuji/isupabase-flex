package io.github.yanfeiwuji.isupabase.auth.vo;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/15 11:54
 */
public record StateTokenInfo(String provider, String referrer) {
    public static StateTokenInfo of(Jwt jwt) {

        final String provider = Optional.ofNullable(jwt.getClaim(AuthStrPool.KEY_PROVIDER)).map(Object::toString).orElse(CharSequenceUtil.EMPTY);
        final String referrer = Optional.ofNullable(jwt.getClaim(AuthStrPool.KEY_REFERRER)).map(Object::toString).orElse(CharSequenceUtil.EMPTY);

        return new StateTokenInfo(provider, referrer);

    }
}
