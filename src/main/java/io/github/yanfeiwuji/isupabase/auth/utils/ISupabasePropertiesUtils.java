package io.github.yanfeiwuji.isupabase.auth.utils;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import io.github.yanfeiwuji.isupabase.config.ISupabaseProperties;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 12:27
 */
@Configuration
@RequiredArgsConstructor
public class ISupabasePropertiesUtils {


    private final ISupabaseProperties isupabaseProperties;

    private static ISupabaseProperties staticIsupabaseProperties;
    private static Map<String, String> weakPasswordMap;
    private static List<Pattern> redirectUrlPatterns;
    private static String siteUrl;


    @PostConstruct
    public void init() {
        ISupabasePropertiesUtils.staticIsupabaseProperties = isupabaseProperties;
        ISupabasePropertiesUtils.weakPasswordMap = Map.of(
                AuthStrPool.WEAK_PASSWORD_LENGTH, "Password should be at least %d characters.".formatted(isupabaseProperties.getPasswordMinLength()),
                AuthStrPool.WEAK_PASSWORD_CHARACTERS, "Password should contain at least one character of each: %s.".formatted(isupabaseProperties.getPasswordRequiredCharacters())
        );
        this.redirectUrlPatterns = isupabaseProperties.getRedirectUrls()
                .stream().map(ISupabasePropertiesUtils::wildcardToRegex)
                .map(Pattern::compile).toList();
        this.siteUrl = isupabaseProperties.getSiteUrl();
    }

    public static String wildcardToRegex(String wildcard) {
        StringBuilder sb = new StringBuilder();
        sb.append("^"); // 开始边界
        for (int i = 0; i < wildcard.length(); i++) {
            char c = wildcard.charAt(i);
            switch (c) {
                case '*':
                    sb.append(".*");
                    break;
                case '.':
                    sb.append("\\.");
                    break;
                case '?':
                    sb.append(".");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        sb.append("$"); // 结束边界
        return sb.toString();
    }


    public static Long passwordMinLength() {
        return staticIsupabaseProperties.getPasswordMinLength();
    }

    public static String passwordRequiredCharacters() {
        return staticIsupabaseProperties.getPasswordRequiredCharacters();
    }

    public static Map<String, String> weakPasswordMap() {
        return weakPasswordMap;
    }

    public static String redirectTo(String redirectTo) {
        final String nonRedirectTo = Optional.ofNullable(redirectTo).orElseGet(ServletUtils::origin);
        String result = Optional.ofNullable(nonRedirectTo)
                .filter(it -> redirectUrlPatterns.stream().anyMatch(pattern -> pattern.matcher(it).matches()) || it.startsWith("http://localhost:"))
                .orElse(siteUrl);
        return CharSequenceUtil.removeSuffix(result, StrPool.SLASH);
    }

}
