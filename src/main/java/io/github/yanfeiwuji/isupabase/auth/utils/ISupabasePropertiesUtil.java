package io.github.yanfeiwuji.isupabase.auth.utils;

import io.github.yanfeiwuji.isupabase.auth.ex.AuthCmExFactory;
import io.github.yanfeiwuji.isupabase.config.ISupabaseProperties;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 12:27
 */
@Configuration
@RequiredArgsConstructor
public class ISupabasePropertiesUtil {
    public record WeakPassword(String mark, String msg) {

    }

    private final ISupabaseProperties isupabaseProperties;
    private static ISupabaseProperties staticIsupabaseProperties;
    private static Map<String, String> weakPasswordMap;

    @PostConstruct
    public void init() {
        ISupabasePropertiesUtil.staticIsupabaseProperties = isupabaseProperties;
        ISupabasePropertiesUtil.weakPasswordMap = Map.of(
                AuthStrPool.WEAK_PASSWORD_LENGTH, "Password should be at least %d characters.".formatted(isupabaseProperties.getPasswordMinLength()),
                AuthStrPool.WEAK_PASSWORD_CHARACTERS, "Password should contain at least one character of each: %s.".formatted(isupabaseProperties.getPasswordRequiredCharacters())
        );
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

}
