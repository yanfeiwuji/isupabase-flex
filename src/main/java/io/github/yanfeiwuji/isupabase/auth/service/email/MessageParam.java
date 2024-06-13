package io.github.yanfeiwuji.isupabase.auth.service.email;

import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 11:50
 */
@Data
@Builder
public class MessageParam {
    private String confirmationURL;
    private String token;
    private String tokenHash;
    private String siteURL;
    private String email;
    private String newEmail;
    private Object data;
    private String redirectTo;

    public static MessageParam of(String siteURL, String email, String tokenHash, String redirectTo) {
        return MessageParam.builder()
                .siteURL(siteURL)
                .email(email)
                .tokenHash(tokenHash)
                .redirectTo(redirectTo).build();
    }

    public void genSignUpConfirmationURL() {
        if (Objects.nonNull(confirmationURL)) {
            return;
        }
        confirmationURL = AuthStrPool.CONFIRM_SIGN_UP_URL_TEMP.formatted(siteURL, tokenHash, redirectTo);
    }

    public void genRecoverConfirmationURL() {
        if (Objects.nonNull(confirmationURL)) {
            return;
        }
        confirmationURL = AuthStrPool.CONFIRM_RECOVER_URL_TEMP.formatted(siteURL, tokenHash, redirectTo);
    }
}
