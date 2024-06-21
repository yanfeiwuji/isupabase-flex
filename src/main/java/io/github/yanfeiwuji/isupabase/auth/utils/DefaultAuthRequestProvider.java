package io.github.yanfeiwuji.isupabase.auth.utils;

import io.github.yanfeiwuji.isupabase.auth.provider.AuthRequestProvider;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthRequest;

import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/15 10:27
 */
@RequiredArgsConstructor
public class DefaultAuthRequestProvider implements AuthRequestProvider {

    @Override
    public Optional<AuthRequest> apply(String s, AuthConfig authConfig) {
        return Optional.ofNullable(s).map(it -> switch (it) {
            case "github" -> new AuthGithubRequest(authConfig);
            case "gitee" -> new AuthGiteeRequest(authConfig);
            default -> null;
        });
    }
}
