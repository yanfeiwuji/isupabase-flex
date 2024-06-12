package io.github.yanfeiwuji.isupabase.auth.service;

import io.github.yanfeiwuji.isupabase.auth.vo.TokenInfo;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author yanfeiwuji
 * @date 2024/6/10 10:21
 */

@Service
@AllArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;


    private final UserDetailsService userDetailsService;
    private final JwtEncoder jwtEncoder;

    public TokenInfo<String> passwordLogin(String username, String password) {
        final Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                "user", "password"
        ));

        final Jwt encode = jwtEncoder.encode(JwtEncoderParameters.from(passwordJwtClaimsSet()));
        final String tokenValue = encode.getTokenValue();
        return TokenInfo.<String>builder().accessToken(tokenValue)
                .refreshToken("refreshToken")
                .user("user")
                .expiresIn(3600L)
                .expiresAt(Objects.requireNonNull(encode.getExpiresAt()).getEpochSecond())
                .tokenType("barer").build();
    }

    private JwtClaimsSet passwordJwtClaimsSet() {
        return JwtClaimsSet.builder()
                .audience(List.of("authenticated"))
                .expiresAt(Instant.now().plusSeconds(3600))
                .issuedAt(Instant.now())
                .issuer("https://github.com")
                .subject("user")
                .claim("email", "1327800522@qq.com")
                .claim("phone", "phone")
                .claim("app_metadata", Map.of("provider", "email"))
                .claim("user_metadata", Map.of("email", "email_verified"))
                .claim("role", "authenticated")
                .claim("aal", "aal1")
                .claim("amr", List.of(Map.of("method", "password")))
                .claim("session_id", "session_id")
                .claim("is_anonymous", false)
                .build();
    }
}
