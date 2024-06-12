package io.github.yanfeiwuji.isupabase.auth.entity;

import com.mybatisflex.annotation.*;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/9 16:10
 */
@EqualsAndHashCode(callSuper = true)
@Table("yfwj_user")
@Data
public class User extends AuthBase implements UserDetails {

    private Long instanceId;
    private String aud;

    private String role;

    private String encryptedPassword;
    private String email;


    private OffsetDateTime emailConfirmedAt;
    private OffsetDateTime invitedAt;
    private String confirmationToken;
    private OffsetDateTime confirmationSentAt;
    private String recoveryToken;
    private String recoverySentAt;

    private String emailChangeTokenNew;

    private String emailChange;
    private String emailChangeSentAt;
    private OffsetDateTime lastSignInAt;

    @Column(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> rawAppMetaData;

    @Column(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> rawUserMetaData;

    private Boolean isSuperAdmin;


    private String phone;

    private String phoneConfirmedAt;
    private String phoneChange;
    private String phoneChangeToken;
    private String phoneChangeSentAt;

    private OffsetDateTime confirmedAt;

    private String emailChangeTokenCurrent;
    private int emailChangeConfirmStatus;

    private OffsetDateTime bannedUntil;

    private String reauthenticationToken;
    private OffsetDateTime reauthenticationSentAt;

    private boolean isSsoUser;
    private String deletedAt;

    private boolean isAnonymous;


    @RelationOneToMany(selfField = "id", targetField = "userId")
    private List<Identity> identities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return encryptedPassword;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
