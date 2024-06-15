package io.github.yanfeiwuji.isupabase.auth.utils;

import io.github.yanfeiwuji.isupabase.config.ISupabaseProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author yanfeiwuji
 * @date 2024/6/15 10:27
 */
@Service
@RequiredArgsConstructor
public class AuthProviderUtils {
    private final ISupabaseProperties iSupabaseProperties;
    private static final Map<String, Function<AuthConfig, AuthRequest>> GEN_REQ_MAP = new HashMap<>();
    private static Map<String, AuthConfig> AUTH_CONFIG_MAP = new HashMap<>();

    static {

        GEN_REQ_MAP.put(AuthDefaultSource.GITHUB.name().toLowerCase(), AuthProviderUtils::githubRequest);
        GEN_REQ_MAP.put(AuthDefaultSource.GITEE.name().toLowerCase(), AuthProviderUtils::giteeRequest);
    }

    @PostConstruct
    private void init() {
        AuthProviderUtils.AUTH_CONFIG_MAP = iSupabaseProperties.getAuthProviders();
    }

    private static AuthGithubRequest githubRequest(AuthConfig config) {
        return new AuthGithubRequest(config);
    }

    private static AuthGiteeRequest giteeRequest(AuthConfig config) {
        return new AuthGiteeRequest(config);
    }

    public static Optional<AuthRequest> authRequest(String provider) {

        return Optional.ofNullable(provider)
                .map(it -> {
                    final AuthConfig authConfig = AUTH_CONFIG_MAP.get(it);
                    final Function<AuthConfig, AuthRequest> authConfigAuthRequestFunction = GEN_REQ_MAP.get(it);
                    if (authConfigAuthRequestFunction == null || authConfig == null) {
                        return null;
                    }
                    return authConfigAuthRequestFunction.apply(authConfig);
                });
    }
}
