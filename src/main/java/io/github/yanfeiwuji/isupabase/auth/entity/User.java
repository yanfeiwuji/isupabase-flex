package io.github.yanfeiwuji.isupabase.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonIgnore
    private Long instanceId;
    private String aud;

    private String role;

    @JsonIgnore
    private String encryptedPassword;
    private String email;


    private OffsetDateTime emailConfirmedAt;

    private OffsetDateTime invitedAt;
    private String confirmationToken;
    private OffsetDateTime confirmationSentAt;
    @JsonIgnore
    private String recoveryToken;

    private String recoverySentAt;

    @JsonIgnore
    private String emailChangeTokenNew;

    @JsonIgnore
    private String emailChange;
    private OffsetDateTime emailChangeSentAt;
    private OffsetDateTime lastSignInAt;

    @Column(typeHandler = JacksonTypeHandler.class)
    @JsonProperty("app_metadata")
    private AppMetaData rawAppMetaData;

    @Column(typeHandler = JacksonTypeHandler.class)
    @JsonProperty("user_metadata")
    private Map<String, Object> rawUserMetaData;
    @JsonIgnore
    private Boolean isSuperAdmin;

    private String phone;

    @JsonIgnore
    private String phoneConfirmedAt;

    @JsonIgnore
    private String phoneChange;
    @JsonIgnore
    private String phoneChangeToken;
    @JsonIgnore
    private String phoneChangeSentAt;

    private OffsetDateTime confirmedAt;

    @JsonIgnore
    private String emailChangeTokenCurrent;
    @JsonInclude
    private int emailChangeConfirmStatus;

    @JsonIgnore
    private OffsetDateTime bannedUntil;

    @JsonIgnore
    private String reauthenticationToken;
    @JsonIgnore
    private OffsetDateTime reauthenticationSentAt;

    @JsonIgnore
    private boolean isSsoUser;
    @JsonIgnore
    private String deletedAt;

    @JsonProperty("is_anonymous")
    private boolean isAnonymous;


    @RelationOneToMany(selfField = "id", targetField = "userId")
    private List<Identity> identities;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return encryptedPassword;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
