package io.github.yanfeiwuji.isupabase.auth.action.param;

import lombok.Data;

import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 16:16
 */
@Data
public class SignUpOptions {
    private String emailRedirectTo;
    private Map<String, Object> data;
    private String captchaToken;
}
