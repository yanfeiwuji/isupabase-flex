package io.github.yanfeiwuji.isupabase.auth.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yanfeiwuji
 * @date 2024/6/9 16:10
 */
@Table("users")
@Data
public class Users {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    private Long instanceId;
    private String aud;

    private String role;

    private String encryptedPassword;
    private String email;


    private LocalDateTime emailConfirmedAt;
    private LocalDateTime invitedAt;
    private String confirmationToken;
    private LocalDateTime confirmationSentAt;
    private String recoveryToken;
    private String recoverySentAt;

    private String emailChangeTokenNew;

    private String emailChange;
    private String emailChangeSentAt;
    private LocalDateTime lastSignInAt;

    private String rawAppMetaData;
    private String rawUserMetaData;

    private Boolean isSuperAdmin;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String phone;

    private String phoneConfirmedAt;
    private String phoneChange;
    private String phoneChangeToken;
    private String phoneChangeSentAt;

    private LocalDateTime confirmedAt;

    private String emailChangeTokenCurrent;
    private int emailChangeConfirmStatus;

    private LocalDateTime bannedUntil;

    private String reauthenticationToken;
    private LocalDateTime reauthenticationSentAt;

    private boolean isSsoUser;
    private String deletedAt;

    private boolean isAnonymous;

}
