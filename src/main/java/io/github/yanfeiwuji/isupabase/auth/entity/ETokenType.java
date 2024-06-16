package io.github.yanfeiwuji.isupabase.auth.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.mybatisflex.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 18:21
 */
@AllArgsConstructor
@Getter
public enum ETokenType {

    CONFIRMATION_TOKEN("confirmation_token"),
    REAUTHENTICATION_TOKEN("reauthentication_token"),
    RECOVERY_TOKEN("recovery_token"),
    EMAIL_CHANGE_TOKEN_NEW("email_change_token_new"),
    EMAIL_CHANGE_TOKEN_CURRENT("email_change_token_current"),
    PHONE_CHANGE_TOKEN("phone_change_token");

    @EnumValue
    @JsonValue
    private final String code;

}
