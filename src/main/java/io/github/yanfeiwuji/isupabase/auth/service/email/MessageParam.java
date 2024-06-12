package io.github.yanfeiwuji.isupabase.auth.service.email;

import lombok.Data;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 11:50
 */
@Data
public class MessageParam {
    private String confirmationURL;
    private String token;
    private String tokenHash;
    private String siteURL;
    private String email;
    private String newEmail;
    private Object data;
    private String redirectTo;
}
