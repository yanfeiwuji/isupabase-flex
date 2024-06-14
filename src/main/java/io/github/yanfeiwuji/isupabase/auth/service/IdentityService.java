package io.github.yanfeiwuji.isupabase.auth.service;

import io.github.yanfeiwuji.isupabase.auth.entity.Identity;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.mapper.IdentityMapper;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static io.github.yanfeiwuji.isupabase.auth.entity.table.IdentityTableDef.IDENTITY;

/**
 * @author yanfeiwuji
 * @date 2024/6/14 17:15
 */
@Service
@RequiredArgsConstructor
public class IdentityService {
    private final IdentityMapper identityMapper;


    public void createEmailIdentity(User user) {
        final Identity identity = new Identity();
        identity.setProviderId(String.valueOf(user.getId()));
        identity.setUserId(user.getId());
        identity.setIdentityData(defaultIdentityDate(user));
        identity.setProvider(AuthStrPool.IDENTITY_PROVIDER_EMAIL);
        identity.setLastSignInAt(OffsetDateTime.now());
        identity.setEmail(user.getEmail());
        identityMapper.insert(identity);
    }

    private Map<String, Object> defaultIdentityDate(User user) {
        TreeMap<String, Object> data = new TreeMap<>();
        data.put("sub", String.valueOf(user.getId()));
        data.put("email", user.getEmail());
        data.put("email_verified", false);
        data.put("phone_verified", false);
        return data;
    }

    private Map<String, Object> emailVerifiedIdentityDate(User user) {
        TreeMap<String, Object> data = new TreeMap<>();
        data.put("sub", String.valueOf(user.getId()));
        data.put("email", user.getEmail());
        data.put("email_verified", true);
        data.put("phone_verified", false);
        return data;
    }

    public void emailVerifiedUserEmail(User user) {
        Identity identity = identityMapper.selectOneByCondition(IDENTITY.USER_ID.eq(user.getId()).and(IDENTITY.PROVIDER.eq(AuthStrPool.IDENTITY_PROVIDER_EMAIL)));
        System.out.println(identity+"---");
        if (Objects.isNull(identity)) {
            // not
            return;
        }
        identity.setIdentityData(emailVerifiedIdentityDate(user));
        identityMapper.update(identity);
    }
}
