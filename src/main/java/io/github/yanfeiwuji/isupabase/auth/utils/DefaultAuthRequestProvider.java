package io.github.yanfeiwuji.isupabase.auth.utils;

import io.github.yanfeiwuji.isupabase.auth.provider.AuthRequestProvider;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthRequest;
import java.util.Map;
import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/15 10:27
 */
@RequiredArgsConstructor
public class DefaultAuthRequestProvider implements AuthRequestProvider {
    private final Map<String, AuthConfig> configMap;


//    private static AuthGithubRequest githubRequest(AuthConfig config) {
//        return new AuthGithubRequest(config);
//    }
//
//    private static AuthGiteeRequest giteeRequest(AuthConfig config) {
//        return new AuthGiteeRequest(config);
//    }
//
//    public static Optional<AuthRequest> authRequest(String provider) {
//
//        return Optional.ofNullable(provider)
//                .map(it -> {
//                    final AuthConfig authConfig = AUTH_CONFIG_MAP.get(it);
//                    final Function<AuthConfig, AuthRequest> authConfigAuthRequestFunction = GEN_REQ_MAP.get(it);
//                    if (authConfigAuthRequestFunction == null || authConfig == null) {
//                        return null;
//                    }
//                    return authConfigAuthRequestFunction.apply(authConfig);
//                });
//    }


    @Override
    public Optional<AuthRequest> apply(String provider) {
        return Optional.ofNullable(configMap).map(it -> it.get(provider))
                .map(it -> switch (provider) {
                    case "github" -> new AuthGithubRequest(it);
                    case "gitee" -> new AuthGiteeRequest(it);
                    default -> null;
                });
    }
}
