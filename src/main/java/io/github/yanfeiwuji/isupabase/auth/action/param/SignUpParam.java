package io.github.yanfeiwuji.isupabase.auth.action.param;

import ch.qos.logback.core.util.StringUtil;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.ex.AuthCmExFactory;
import io.github.yanfeiwuji.isupabase.auth.utils.ValueValidUtil;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 11:59
 */
@Data
public class SignUpParam {

    private String codeChallenge;
    private String codeChallengeMethod;
    private Map<String, Object> data;
    @Email
    private String email;
    private String password;
    //
    private String phone;
    //
    private String channel;
    // get redirect to
    private String redirectTo;

    public User toUser() {
        final User user = new User();
        user.setRole(AuthStrPool.AUTHENTICATED_ROLE);
        user.setAud(AuthStrPool.AUTHENTICATED_AUD);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAnonymous(false);
        return user;
    }

    public SignUpParam validPassword() {
        Optional.ofNullable(password)
                .orElseThrow(AuthCmExFactory::validatePassword);
        return this;
    }

    public SignUpParam validEmail() {
        Optional.ofNullable(email)
                .filter(ValueValidUtil::isEmail)
                .orElseThrow(() -> AuthCmExFactory.VALIDATION_FAILED_EMAIL);
        return this;
    }

    public SignUpParam validPhone() {

        throw AuthCmExFactory.PHONE_PROVIDER_DISABLE;
    }

    public SignUpParam valid() {
        validPassword();
        if (StringUtil.notNullNorEmpty(phone)) {
            validPhone();
        } else {
            validEmail();
        }
        return this;

    }
}
