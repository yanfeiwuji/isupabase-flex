package io.github.yanfeiwuji.isupabase.auth.service;

import cn.hutool.core.lang.id.NanoId;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import io.github.yanfeiwuji.isupabase.auth.action.param.RecoverParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.SignUpParam;
import io.github.yanfeiwuji.isupabase.auth.entity.*;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.event.RecoverEvent;
import io.github.yanfeiwuji.isupabase.auth.event.SignUpEvent;
import io.github.yanfeiwuji.isupabase.auth.ex.AuthCmExFactory;
import io.github.yanfeiwuji.isupabase.auth.ex.AuthExFactory;
import io.github.yanfeiwuji.isupabase.auth.ex.AuthExRes;
import io.github.yanfeiwuji.isupabase.auth.mapper.*;
import io.github.yanfeiwuji.isupabase.auth.utils.AuthUtil;
import io.github.yanfeiwuji.isupabase.auth.vo.TokenInfo;
import io.github.yanfeiwuji.isupabase.config.ISupabaseProperties;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import io.micrometer.core.instrument.step.StepTuple2;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

import static io.github.yanfeiwuji.isupabase.auth.entity.table.IdentityTableDef.IDENTITY;
import static io.github.yanfeiwuji.isupabase.auth.entity.table.RefreshTokenTableDef.REFRESH_TOKEN;
import static io.github.yanfeiwuji.isupabase.auth.entity.table.SessionTableDef.SESSION;
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
    private final JwtEncoder jwtEncoder;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SessionMapper sessionMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final IdentityMapper identityMapper;
    //  private  final AuthMimeMessagePreparationFactory mimeMessagePreparationFactory;
    private final ApplicationEventPublisher publisher;

    private final OneTimeTokenMapper oneTimeTokenMapper;
    private final OneTimeTokenService oneTimeTokenService;
    private final JWTService jwtService;

    private Long jwtExp;
    private Long passwordMinLength;
    private String passwordRequiredCharacters;
    private Long oneTimeExpiredMinutes;


    @PostConstruct
    public void init() {
        this.jwtExp = isupabaseProperties.getJwtExp();
        this.passwordMinLength = isupabaseProperties.getPasswordMinLength();
        this.passwordRequiredCharacters = isupabaseProperties.getPasswordRequiredCharacters();
        this.oneTimeExpiredMinutes = isupabaseProperties.getOneTimeExpiredMinutes();
    }


    public TokenInfo<User> passwordLogin(String username, String password) {

        final Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                username, password
        ));
        //
        final User principal = (User) authenticate.getPrincipal();
        final OffsetDateTime emailConfirmedAt = principal.getEmailConfirmedAt();

        if (Objects.isNull(emailConfirmedAt)) {
            throw AuthExFactory.INVALID_GRANT_EMAIL_NOT_CONFIRMED;
        }

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
            userMapper.insert(newUser);

            publisher.publishEvent(new SignUpEvent(this, newUser, signUpParam));
            return newUser;
        } else {

            final List<Identity> identities = identityMapper.selectListByCondition(IDENTITY.USER_ID.eq(user.getId()));
            user.setIdentities(identities);
            user.setConfirmationSentAt(OffsetDateTime.now());

            publisher.publishEvent(new SignUpEvent(this, user, signUpParam));
            userMapper.update(user);
            return user;
        }
    }

    public void singUpByPhone(String email) {

    }

    //  @SneakyThrows
    public void recover(RecoverParam recoverParam) {

        Optional.ofNullable(recoverParam).map(RecoverParam::getEmail)
                .map(USER.EMAIL::eq)
                .map(userMapper::selectOneByCondition)
                .map(it -> new RecoverEvent(this, it, recoverParam))
                .ifPresent(publisher::publishEvent);
    }


    public void logoutGlobal() {
        final Optional<Long> uid = AuthUtil.uid();
        if (uid.isEmpty()) {
            return;
        }
        sessionMapper.deleteByCondition(SESSION.USER_ID.eq(uid.get()));
        refreshTokenMapper.deleteByCondition(REFRESH_TOKEN.USER_ID.eq(uid.get()));
    }

    public void logoutLocal() {
        final Optional<Long> sessionId = AuthUtil.sessionId();

        if (sessionId.isEmpty()) {
            return;
        }

        sessionMapper.deleteById(sessionId.get());
        refreshTokenMapper.deleteByCondition(REFRESH_TOKEN.SESSION_ID.eq(sessionId.get()));
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
        final User user = userMapper.selectOneById(userId);

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
        if (Objects.isNull(user)) {
            return;
        }
        final OffsetDateTime confirmationSentAt = user.getConfirmationSentAt();
        if (confirmationSentAt == null || confirmationSentAt.plusMinutes(oneTimeExpiredMinutes).isAfter(OffsetDateTime.now())) {
            return;
        }
        final OffsetDateTime now = OffsetDateTime.now();
        user.setConfirmedAt(now);
        user.setEmailConfirmedAt(now);
        user.setConfirmationToken(null);
        userMapper.update(user);
    }


    public Optional<TokenInfo<User>> verifyRecovery(OneTimeToken oneTimeToken) {

        return Optional.ofNullable(oneTimeToken)
                .map(OneTimeToken::getUserId)
                .map(userMapper::selectOneById)
                .map(jwtService::userToOTPTokenInfo);

    }
}
