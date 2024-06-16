package io.github.yanfeiwuji.isupabase.auth.service;

import cn.hutool.core.lang.id.NanoId;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import io.github.yanfeiwuji.isupabase.auth.action.param.PutUserParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.RecoverParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.SignUpParam;
import io.github.yanfeiwuji.isupabase.auth.entity.*;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.event.*;
import io.github.yanfeiwuji.isupabase.auth.ex.AuthCmExFactory;
import io.github.yanfeiwuji.isupabase.auth.ex.AuthExFactory;
import io.github.yanfeiwuji.isupabase.auth.mapper.*;
import io.github.yanfeiwuji.isupabase.auth.utils.AuthUtils;
import io.github.yanfeiwuji.isupabase.auth.utils.ValueValidUtils;
import io.github.yanfeiwuji.isupabase.auth.vo.TokenInfo;
import io.github.yanfeiwuji.isupabase.config.ISupabaseProperties;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

import static io.github.yanfeiwuji.isupabase.auth.entity.table.IdentityTableDef.IDENTITY;
import static io.github.yanfeiwuji.isupabase.auth.entity.table.RefreshTokenTableDef.REFRESH_TOKEN;

import static io.github.yanfeiwuji.isupabase.auth.entity.table.UserTableDef.USER;

/**
 * @author yanfeiwuji
 * @date 2024/6/10 10:21
 */

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final ISupabaseProperties isupabaseProperties;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SessionMapper sessionMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final IdentityMapper identityMapper;
    private final ApplicationEventPublisher publisher;

    private final JWTService jwtService;

    private Long passwordMinLength;
    private String passwordRequiredCharacters;

    @PostConstruct
    public void init() {
        this.passwordMinLength = isupabaseProperties.getPasswordMinLength();
        this.passwordRequiredCharacters = isupabaseProperties.getPasswordRequiredCharacters();

    }

    public TokenInfo<User> passwordLogin(String username, String password) {

        final Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                username, password));
        //
        final User principal = (User) authenticate.getPrincipal();
        final OffsetDateTime emailConfirmedAt = principal.getEmailConfirmedAt();

        if (Objects.isNull(emailConfirmedAt)) {
            throw AuthExFactory.INVALID_GRANT_EMAIL_NOT_CONFIRMED;
        }
        final List<Identity> identities = identityMapper.selectListByCondition(IDENTITY.USER_ID.eq(principal.getId()));
        principal.setIdentities(identities);
        publisher.publishEvent(new FetchTokenEvent(this, principal));
        return jwtService.userToTokenInfo(principal);
    }

    public User singUpByEmail(SignUpParam signUpParam) {
        validPassword(signUpParam.getPassword());
        final User user = userMapper.selectOneByCondition(USER.EMAIL.eq(signUpParam.getEmail()));
        if (Objects.isNull(user)) {
            // reg
            final User newUser = signUpParam.toUser();
            newUser.setEncryptedPassword(passwordEncoder.encode(signUpParam.getPassword()));

            newUser.setConfirmationToken(NanoId.randomNanoId());
            newUser.setConfirmationSentAt(OffsetDateTime.now());
            newUser.setRawAppMetaData(AppMetaData.EMAIL_APP_META_DATA);

            userMapper.insert(newUser);
            publisher.publishEvent(new SignUpEvent(this, newUser, signUpParam));
            return newUser;
        } else {

            if (Objects.isNull(user.getEmailConfirmedAt())) {
                user.setConfirmationSentAt(OffsetDateTime.now());
                publisher.publishEvent(new SignUpEvent(this, user, signUpParam));
                userMapper.update(user);
            }
            return user;
        }
    }

    public void singUpByPhone(String email) {
        // todo
    }

    // @SneakyThrows
    public void recover(RecoverParam recoverParam) {

        Optional.ofNullable(recoverParam).map(RecoverParam::getEmail)
                .map(USER.EMAIL::eq)
                .map(userMapper::selectOneByCondition)
                .map(it -> new RecoverEvent(this, it, recoverParam))
                .ifPresent(publisher::publishEvent);
    }

    public TokenInfo<User> refreshToken(String refreshToken) {
        final RefreshToken token = refreshTokenMapper.selectOneByCondition(REFRESH_TOKEN.TOKEN.eq(refreshToken));
        if (Objects.isNull(token)) {
            throw AuthExFactory.INVALID_GRANT_REFRESH_TOKEN_NOT_FOUND;
        }

        if (BooleanUtil.isTrue(token.getRevoked())) {
            throw AuthExFactory.INVALID_GRANT_ALREADY_USED;
        }

        final Long sessionId = token.getSessionId();

        final Session session = sessionMapper.selectOneById(sessionId);
        if (Objects.isNull(session)) {
            refreshTokenMapper.deleteByCondition(REFRESH_TOKEN.SESSION_ID.eq(sessionId));
            throw AuthExFactory.INVALID_GRANT_REFRESH_TOKEN_NOT_FOUND;
        }

        session.setRefreshedAt(OffsetDateTime.now());

        sessionMapper.update(session);

        token.setRevoked(true);
        final RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setRevoked(false);
        newRefreshToken.setUserId(token.getUserId());
        newRefreshToken.setParent(token.getToken());
        newRefreshToken.setToken(NanoId.randomNanoId());
        newRefreshToken.setSessionId(session.getId());
        refreshTokenMapper.insert(newRefreshToken);

        final Long userId = session.getUserId();
        final User user = userMapper.selectOneWithRelationsById(userId);

        publisher.publishEvent(new FetchTokenEvent(this, user));
        return jwtService.userToTokenInfo(user, session, newRefreshToken);
    }

    private void validPassword(String password) {

        List<String> reason = new ArrayList<>(2);
        if (password.length() < passwordMinLength) {
            reason.add(AuthStrPool.WEAK_PASSWORD_LENGTH);
        }
        if (!CharSequenceUtil.isEmpty(passwordRequiredCharacters)) {
            final String[] split = passwordRequiredCharacters.split("(?<!\\\\):");
            boolean has = Arrays.stream(split).allMatch(it -> {
                final char[] charArray = it.toCharArray();
                for (char c : charArray) {
                    if (password.indexOf(c) >= 0) {
                        return true;
                    }
                }
                return false;
            });
            if (!has) {
                reason.add(AuthStrPool.WEAK_PASSWORD_CHARACTERS);
            }
        }
        if (!reason.isEmpty()) {
            throw AuthCmExFactory.weakPassword(reason);
        }

    }

    public void verifyConfirmationToken(OneTimeToken oneTimeToken) {
        final User user = userMapper.selectOneById(oneTimeToken.getUserId());

        final OffsetDateTime now = OffsetDateTime.now();
        user.setConfirmedAt(now);
        user.setEmailConfirmedAt(now);
        user.setConfirmationToken(null);
        publisher.publishEvent(new EmailVerifiedEvent(this, user));
        userMapper.update(user);
    }

    public User putUser(PutUserParam putUserParam) {
        Long uid = AuthUtils.uid().orElseThrow(() -> new AccessDeniedException(CharSequenceUtil.EMPTY));
        final User user = userMapper.selectOneById(uid);

        Optional.ofNullable(putUserParam).map(PutUserParam::getPassword).ifPresent(pwd -> {
            validPassword(pwd);
            Optional.ofNullable(user.getPassword())
                    .ifPresent(userCurrentPwd -> {
                        if (passwordEncoder.matches(pwd, userCurrentPwd)) {
                            throw AuthCmExFactory.SAME_PASSWORD;
                        }
                    });
            final String encodeNewPassword = passwordEncoder.encode(pwd);
            // set new password
            user.setEncryptedPassword(encodeNewPassword);
            publisher.publishEvent(new ChangePasswordEvent(this, user));
        });
        // change email
        Optional.ofNullable(putUserParam).map(PutUserParam::getEmail)
                .filter(ValueValidUtils::isEmail)
                .ifPresent(email -> {
                    final long countEmail = userMapper.selectCountByCondition(USER.EMAIL.eq(email));
                    if (countEmail > 0) {
                        throw AuthCmExFactory.EMAIL_EXISTS;
                    }

                    user.setEmailChange(email);
                    user.setEmailChangeSentAt(OffsetDateTime.now());
                    user.setEmailChangeTokenCurrent(NanoId.randomNanoId());
                    user.setEmailChangeTokenNew(NanoId.randomNanoId());
                    user.setEmailChangeConfirmStatus(0);
                    publisher.publishEvent(new ChangeEmailEvent(this, user, putUserParam.getRedirectTo()));
                });

        Optional.ofNullable(putUserParam).map(PutUserParam::getData)
                .ifPresent(user::setRawUserMetaData);

        userMapper.update(user);
        return user;
    }

    public Integer verifyEmailChange(OneTimeToken oneTimeToken) {
        final Optional<User> userOptional = Optional.ofNullable(oneTimeToken).map(OneTimeToken::getUserId)
                .map(userMapper::selectOneById);
        if (userOptional.isPresent()) {
            final User user = userOptional.get();
            final int status = user.getEmailChangeConfirmStatus();
            switch (status) {
                case 0 -> {
                    user.setEmailChangeConfirmStatus(1);
                    userMapper.update(user);
                    return 1;
                }
                case 1 -> {
                    user.setEmailChangeConfirmStatus(0);
                    final String emailChange = user.getEmailChange();
                    final long l = userMapper.selectCountByCondition(USER.EMAIL.eq(emailChange));
                    if (l > 0) {
                        return -1;
                    }
                    user.setEmail(emailChange);
                    userMapper.update(user);
                    return 0;
                }
                default -> {
                    return null;
                }
            }

        }
        return null;
    }

    public User identityConfirmByEmail(String provider, AuthUser authUser) {

        User user = userMapper.selectOneByCondition(USER.EMAIL.eq(authUser.getEmail()));
        if (Objects.isNull(user)) {
            user = new User();
            user.setEmail(authUser.getEmail());
            user.setRole(AuthStrPool.AUTHENTICATED_ROLE);
            user.setAud(AuthStrPool.AUTHENTICATED_AUD);
            user.setEmail(authUser.getEmail());
            user.setAnonymous(false);
            user.setRawUserMetaData(
                    Map.of(AuthStrPool.KEY_PROVIDER, provider, AuthStrPool.KEY_PROVIDERS, List.of(provider)));
            user.setEmailConfirmedAt(OffsetDateTime.now());
            userMapper.insert(user);
        }
        publisher.publishEvent(new OauthIdentityConfirmEvent(this, user, provider, authUser));
        return user;
    }
}
