package io.github.yanfeiwuji.isupabase.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.github.yanfeiwuji.isupabase.auth.ex.AuthExRes;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


/**
 * @author yanfeiwuji
 * @date 2024/6/10 09:39
 */

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final ISupabaseProperties properties;


    @Bean
    @Order(1)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, ObjectMapper mapper)
            throws Exception {
        http
                .authorizeHttpRequests(authorize -> {
                            authorize.requestMatchers("auth/v1/verify", "/auth/v1/authorize", "/auth/v1/callback").permitAll();
                            //     authorize.requestMatchers("/auth/v1/token", "/auth/v1/authorize").authenticated();
                            authorize.anyRequest().authenticated();
                        }
                ).
                csrf(AbstractHttpConfigurer::disable);

        http.oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults()))
                .exceptionHandling(exceptions ->
                        exceptions.defaultAccessDeniedHandlerFor(
                                        new GoTureAccessDeniedHandler(mapper),
                                        new MediaTypeRequestMatcher(MediaType.ALL)
                                )
                                .defaultAuthenticationEntryPointFor(
                                        new GoTureAuthenticationEntryPoint(mapper),
                                        new MediaTypeRequestMatcher(MediaType.ALL)
                                )
                )
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }


    @ConditionalOnMissingBean
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            byte[] privateKeyBytes = Base64.getDecoder().decode(properties.getAuthPrivateKey()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("\n", ""));
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

            byte[] publicKeyBytes = Base64.getDecoder().decode(properties.getAuthPublicKey()
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("\n", ""));
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            RSAKey rsaKey = new RSAKey.
                    Builder((RSAPublicKey) publicKey)
                    .privateKey((RSAPrivateKey) privateKey)
                    .keyID("yanfeiwuji")

                    .build();
            JWKSet jwkSet = new JWKSet(rsaKey);
            return new ImmutableJWKSet<>(jwkSet);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA keys", e);
        }

    }


    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {

        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    private record GoTureAccessDeniedHandler(ObjectMapper mapper) implements AccessDeniedHandler {

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            final PrintWriter out = response.getWriter();
            out.write(mapper.writeValueAsString(AuthExRes.INVALID_GRANT));
            out.flush();
            out.close();
        }
    }

    private record GoTureAuthenticationEntryPoint(ObjectMapper mapper) implements AuthenticationEntryPoint {

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            final PrintWriter out = response.getWriter();
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            out.write(mapper.writeValueAsString(AuthExRes.INVALID_GRANT));
            out.flush();
            out.close();
        }
    }


    public static void main(String[] args) {

    }
}
