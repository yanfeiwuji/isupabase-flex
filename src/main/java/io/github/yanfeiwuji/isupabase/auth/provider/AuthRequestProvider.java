package io.github.yanfeiwuji.isupabase.auth.provider;

import me.zhyd.oauth.request.AuthRequest;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author yanfeiwuji
 * @date 2024/6/20 17:06
 */
public interface AuthRequestProvider extends Function<String, Optional<AuthRequest>> {
}
