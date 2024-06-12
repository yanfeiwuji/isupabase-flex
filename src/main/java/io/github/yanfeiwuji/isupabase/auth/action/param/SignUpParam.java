package io.github.yanfeiwuji.isupabase.auth.action.param;

import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import lombok.Data;

import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 11:59
 */
@Data
public class SignUpParam {
    private String codeChallenge;
    private String codeChallengeMethod;
    private Map<String, Object> data;
    private String email;
    private String password;
    //
    private String phone;
    //
    private String channel;

    public User toUser() {
        final User user = new User();
        user.setRole(AuthStrPool.AUTHENTICATED_ROLE);
        user.setAud(AuthStrPool.AUTHENTICATED_AUD);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAnonymous(false);
        return user;
    }
}
