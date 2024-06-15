package io.github.yanfeiwuji.isupabase.auth.service;

import io.github.yanfeiwuji.isupabase.auth.entity.AppMetaData;
import io.github.yanfeiwuji.isupabase.auth.entity.Identity;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.mapper.IdentityMapper;
import io.github.yanfeiwuji.isupabase.auth.mapper.UserMapper;
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
    private final UserMapper userMapper;





//    public void emailVerifiedUserEmail(User user) {
//        Identity identity = identityMapper.selectOneByCondition(IDENTITY.USER_ID.eq(user.getId()).and(IDENTITY.PROVIDER.eq(AuthStrPool.IDENTITY_PROVIDER_EMAIL)));
//
//        if (Objects.isNull(identity)) {
//            // not
//            return;
//        }
//
//        identity.setIdentityData(emailVerifiedIdentityDate(user));
//        updateUserIdentity(user, identity);
//
//        identityMapper.update(identity);
//    }

    public void identityConfirm(User user, String provider, String providerId, Map<String, Object> identityData) {
        Identity identity = identityMapper.selectOneByCondition(IDENTITY.USER_ID.eq(user.getId()).and(IDENTITY.PROVIDER.eq(provider)));
        if (Objects.isNull(identity)) {
            identity = new Identity();
            identity.setProviderId(providerId);
            identity.setUserId(user.getId());
            identity.setIdentityData(fillEmailVerifiedIdentityData(providerId, user, identityData));
            identity.setProvider(provider);
            identity.setLastSignInAt(OffsetDateTime.now());
            identity.setEmail(user.getEmail());
        }
        updateUserIdentity(user, identity);
        identityMapper.insertOrUpdate(identity);
    }

    private void updateUserIdentity(User user, Identity identity) {
        final AppMetaData rawAppMetaData = user.getRawAppMetaData();
        final AppMetaData appMetaData = AppMetaData.addProvider(rawAppMetaData, identity.getProvider());
        user.setRawAppMetaData(appMetaData);
        user.setRawUserMetaData(identity.getIdentityData());
        userMapper.update(user);
    }

    private Map<String, Object> fillEmailVerifiedIdentityData(String providerId, User user, Map<String, Object> identityData) {
        TreeMap<String, Object> data = new TreeMap<>(identityData);
        data.put("sub", String.valueOf(providerId));
        data.put("email", user.getEmail());
        data.put("email_verified", true);
        data.put("phone_verified", false);
        return data;
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
}