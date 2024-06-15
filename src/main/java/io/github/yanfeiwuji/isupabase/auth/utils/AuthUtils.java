package io.github.yanfeiwuji.isupabase.auth.utils;

import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;
import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/10 17:01
 */
@UtilityClass
public class AuthUtils {
    public Optional<Jwt> jwt() {
        return Optional.of(SecurityContextHolder.getContext()).map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .filter(Jwt.class::isInstance)
                .map(Jwt.class::cast);
    }

    public String role() {
        return Optional.of(SecurityContextHolder.getContext()).map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .filter(Jwt.class::isInstance)
                .map(Jwt.class::cast)
                .map(Jwt::getClaims)
                .map(it -> it.get(AuthStrPool.JWT_ROLE_KEY))
                .map(Object::toString)
                .orElse(AuthStrPool.ANON_ROLE);
    }

    public boolean isAuthenticated() {
        return role().equals(AuthStrPool.AUTHENTICATED_ROLE);
    }

    public boolean isAnon() {
        return role().equals(AuthStrPool.ANON_ROLE);
    }

    public Optional<Long> uid() {
        return Optional.of(SecurityContextHolder.getContext()).map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .filter(Jwt.class::isInstance)
                .map(Jwt.class::cast)
                .map(Jwt::getSubject)
                .map(Long::valueOf);
    }

    public Optional<Long> sessionId() {
        return Optional.of(SecurityContextHolder.getContext()).map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .filter(Jwt.class::isInstance)
                .map(Jwt.class::cast)
                .map(Jwt::getClaims)
                .map(it -> it.get("session_id"))
                .map(Objects::toString)
                .map(Long::valueOf);
    }


}
