package io.github.yanfeiwuji.isupabase.auth.action.param;

import lombok.Data;

import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 10:07
 */
@Data
public class RecoverParam {
    private String email;
    private String codeChallenge;
    private String codeChallengeMethod;
    // todo
    private Map<String, Object> gotrueMetaSecurity;
    private String redirectTo;
}
