package io.github.yanfeiwuji.isupabase.auth.service;

import cn.hutool.core.lang.id.NanoId;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.yanfeiwuji.isupabase.auth.entity.*;
import io.github.yanfeiwuji.isupabase.auth.event.EmailVerifiedEvent;
import io.github.yanfeiwuji.isupabase.auth.mapper.RefreshTokenMapper;
import io.github.yanfeiwuji.isupabase.auth.mapper.SessionMapper;
import io.github.yanfeiwuji.isupabase.auth.mapper.UserMapper;
import io.github.yanfeiwuji.isupabase.auth.utils.ServletUtils;
import io.github.yanfeiwuji.isupabase.auth.vo.StateTokenInfo;
import io.github.yanfeiwuji.isupabase.auth.vo.TokenInfo;
import io.github.yanfeiwuji.isupabase.config.ISupabaseProperties;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/14 09:45
 */
@Service
@RequiredArgsConstructor
public class JWTService {

    private final ISupabaseProperties isupabaseProperties;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final SessionMapper sessionMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher publisher;

    private Long jwtExp;
    private String siteUrl;

    private record Amr(String method, Long timestamp) {
        public Amr(String method) {
            this(method, System.currentTimeMillis());
        }

        public static List<Amr> of(List<String> methods) {
            return methods.stream().map(Amr::new).toList();
        }

    }

    @PostConstruct
    public void init() {
        this.jwtExp = isupabaseProperties.getJwtExp();
        this.siteUrl = isupabaseProperties.getSiteUrl();
    }

    public Optional<TokenInfo<User>> oneTimeTokenToOTPTokenInfo(OneTimeToken oneTimeToken) {
        final Optional<TokenInfo<User>> userTokenInfoOptional = Optional.ofNullable(oneTimeToken)
                .map(OneTimeToken::getUserId)
                .map(userMapper::selectOneById)
                .map(this::userToOTPTokenInfo);
        userTokenInfoOptional.map(TokenInfo::getUser).map(it -> new EmailVerifiedEvent(this, it))
                .ifPresent(publisher::publishEvent);
        return userTokenInfoOptional;

    }

    public TokenInfo<User> userToTokenInfo(User user, Session session, RefreshToken refreshToken, List<String> amr) {
        final JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .audience(List.of(AuthStrPool.AUTHENTICATED_AUD))
                .expiresAt(Instant.now().plusSeconds(jwtExp))
                .issuedAt(Instant.now())
                .issuer("https://github.com")
                .subject(user.getId().toString())
                .claim("email", Optional.ofNullable(user.getEmail()).orElse(CharSequenceUtil.EMPTY))
                .claim("phone", Optional.ofNullable(user.getPhone()).orElse(CharSequenceUtil.EMPTY))
                .claim("user_metadata", Optional.ofNullable(user.getRawUserMetaData()).orElse(Map.of()))
                .claim("role", Optional.ofNullable(user.getRole()).orElse(AuthStrPool.ANON_ROLE))
                .claim("aal",
                        Optional.of(session).map(Session::getAal).map(EAalLevel::getCode)
                                .orElse(EAalLevel.ALL_1.getCode()))
                .claim("amr", Amr.of(amr))
                .claim("session_id", String.valueOf(session.getId()))
                .claim("is_anonymous", user.isAnonymous())
                .claims(map -> map.put("app_metadata", user.getRawAppMetaData()))
                .build();

        final Jwt encode = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet));
        final String tokenValue = encode.getTokenValue();
        return TokenInfo.<User>builder().accessToken(tokenValue)
                .refreshToken(refreshToken.getToken())
                .user(user)
                .expiresIn(jwtExp)
                .expiresAt(Objects.requireNonNull(encode.getExpiresAt()).getEpochSecond())
                .tokenType(AuthStrPool.TOKE_TYPE).build();
    }

    public TokenInfo<User> userToTokenInfo(User user, List<String> amr) {

        Objects.requireNonNull(user);
        final Session session = new Session();
        session.setUserId(user.getId());
        session.setAal(EAalLevel.ALL_1);
        session.setUserAgent(ServletUtils.userAgent());
        session.setIp(ServletUtils.ip());
        sessionMapper.insert(session);

        final RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRevoked(false);
        refreshToken.setUserId(user.getId());
        refreshToken.setToken(NanoId.randomNanoId());
        refreshToken.setSessionId(session.getId());
        refreshTokenMapper.insert(refreshToken);
        return this.userToTokenInfo(user, session, refreshToken, amr);
    }

    public TokenInfo<User> userToTokenInfo(User user) {
        return this.userToTokenInfo(user, List.of(AuthStrPool.PASSWORD));
    }

    public TokenInfo<User> userToTokenInfo(User user, Session session, RefreshToken refreshToken) {
        return userToTokenInfo(user, session, refreshToken, List.of(AuthStrPool.PASSWORD));
    }

    public TokenInfo<User> userToOTPTokenInfo(User user) {
        return userToTokenInfo(user, List.of(AuthStrPool.OTP));
    }

    public String stateToken(String provider, String referrer) {
        final JwtClaimsSet claimsSet = JwtClaimsSet.builder().expiresAt(Instant.now().plusSeconds(jwtExp))
                .claim(AuthStrPool.KEY_SITE_URL, siteUrl)
                // from supabase
                .claim(AuthStrPool.KEY_STATE_TOKEN_ID, AuthStrPool.VALUE_STATE_TOKEN_ID)
                .claim(AuthStrPool.KEY_PROVIDER, provider)
                .claim(AuthStrPool.KEY_REFERRER, referrer)
                .claim(AuthStrPool.KEY_FLOW_STATE_ID, CharSequenceUtil.EMPTY)
                .claims(m -> m.put(AuthStrPool.KEY_FUNCTION_HOOKS, null))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    public StateTokenInfo decodeStateToken(String token) {
        final Jwt jwt = jwtDecoder.decode(token);
        // final Instant expiresAt = jwt.getExpiresAt();

        return StateTokenInfo.of(jwt);
    }

}
