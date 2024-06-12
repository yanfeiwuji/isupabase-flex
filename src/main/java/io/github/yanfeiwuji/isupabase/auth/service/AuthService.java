package io.github.yanfeiwuji.isupabase.auth.service;

import cn.hutool.core.lang.id.NanoId;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.yanfeiwuji.isupabase.auth.action.param.SignUpParam;
import io.github.yanfeiwuji.isupabase.auth.entity.*;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.entity.table.RefreshTokenTableDef;
import io.github.yanfeiwuji.isupabase.auth.mapper.RefreshTokenMapper;
import io.github.yanfeiwuji.isupabase.auth.mapper.SessionMapper;
import io.github.yanfeiwuji.isupabase.auth.mapper.UserMapper;
import io.github.yanfeiwuji.isupabase.auth.utils.AuthUtil;
import io.github.yanfeiwuji.isupabase.auth.utils.ServletUtil;
import io.github.yanfeiwuji.isupabase.auth.vo.TokenInfo;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import io.github.yanfeiwuji.isupabase.entity.SysUser;
import io.github.yanfeiwuji.isupabase.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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


    private final UserDetailsService userDetailsService;
    private final JwtEncoder jwtEncoder;
    private final JavaMailSender mailSender;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SessionMapper sessionMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final SysUserMapper sysUserMapper;


    public TokenInfo<User> passwordLogin(String username, String password) {

        final Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                username, password
        ));
        //
        final User principal = (User) authenticate.getPrincipal();
        final Session session = new Session();
        session.setUserId(principal.getId());
        session.setAal(EAalLevel.ALL_1);
        session.setUserAgent(ServletUtil.userAgent());
        session.setIp(ServletUtil.ip());
        sessionMapper.insert(session);

        final RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRevoked(false);
        refreshToken.setUserId(principal.getId());
        refreshToken.setToken(NanoId.randomNanoId());
        refreshToken.setSessionId(session.getId());
        refreshTokenMapper.insert(refreshToken);

        return UserToJwtClaimsSet(principal, session, refreshToken);
    }


    public User singUpByEmail(SignUpParam signUpParam) {

        final User User = userMapper.selectOneByCondition(USER.EMAIL.eq(signUpParam.getEmail()));
        if (Objects.isNull(User)) {
            // reg
            final User newUser = signUpParam.toUser();
            newUser.setEncryptedPassword(passwordEncoder.encode(signUpParam.getPassword()));
            userMapper.insert(newUser);
            return newUser;
        } else {
            return userMapper.selectOneWithRelationsByCondition(USER.EMAIL.eq(signUpParam.getEmail()));
        }
    }

    public void singUpByPhone(String email) {

    }

    //  @SneakyThrows
    public void recover(String email) {

    }


    private TokenInfo<User> UserToJwtClaimsSet(User user, Session session, RefreshToken refreshToken) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(session);
        Objects.requireNonNull(refreshToken);

        final JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .audience(List.of(AuthStrPool.AUTHENTICATED_AUD))
                .expiresAt(Instant.now().plusSeconds(3600))
                .issuedAt(Instant.now())
                .issuer("https://github.com")
                .subject(user.getId().toString())
                .claim("email", Optional.ofNullable(user.getEmail()).orElse(CharSequenceUtil.EMPTY))
                .claim("phone", Optional.ofNullable(user.getPhone()).orElse(CharSequenceUtil.EMPTY))
                .claim("app_metadata", Optional.ofNullable(user.getRawAppMetaData()).orElse(Map.of()))
                .claim("user_metadata", Optional.ofNullable(user.getRawAppMetaData()).orElse(Map.of()))
                .claim("role", Optional.ofNullable(user.getRole()).orElse(AuthStrPool.ANON_ROLE))
                .claim("aal", Optional.of(session).map(Session::getAal).map(EAalLevel::getCode).orElse(EAalLevel.ALL_1.getCode()))
                .claim("amr", List.of(Map.of("method", "password")))
                .claim("session_id", session.getId())
                .claim("is_anonymous", user.isAnonymous())
                .build();
        final Jwt encode = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet));
        final String tokenValue = encode.getTokenValue();
        return TokenInfo.<User>builder().accessToken(tokenValue)
                .refreshToken(refreshToken.getToken())
                .user(user)
                .expiresIn(3600L)
                .expiresAt(Objects.requireNonNull(encode.getExpiresAt()).getEpochSecond())
                .tokenType("barer").build();

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
        System.out.println(sessionId);
        if (sessionId.isEmpty()) {
            return;
        }

        sessionMapper.deleteById(sessionId.get());
        refreshTokenMapper.deleteByCondition(REFRESH_TOKEN.SESSION_ID.eq(sessionId.get()));
    }
}
