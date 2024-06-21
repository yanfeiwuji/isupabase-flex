package io.github.yanfeiwuji.isupabase.auth.action;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.yanfeiwuji.isupabase.auth.action.param.PutUserParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.RecoverParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.SignUpParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.TokenParam;
import io.github.yanfeiwuji.isupabase.auth.entity.ETokenType;
import io.github.yanfeiwuji.isupabase.auth.entity.OneTimeToken;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.ex.AuthCmExFactory;
import io.github.yanfeiwuji.isupabase.auth.ex.AuthExFactory;
import io.github.yanfeiwuji.isupabase.auth.mapper.UserMapper;
import io.github.yanfeiwuji.isupabase.auth.provider.AuthRequestProvider;
import io.github.yanfeiwuji.isupabase.auth.service.AuthService;
import io.github.yanfeiwuji.isupabase.auth.service.JWTService;
import io.github.yanfeiwuji.isupabase.auth.service.OneTimeTokenService;
import io.github.yanfeiwuji.isupabase.auth.service.SessionService;
import io.github.yanfeiwuji.isupabase.auth.utils.*;
import io.github.yanfeiwuji.isupabase.auth.vo.StateTokenInfo;
import io.github.yanfeiwuji.isupabase.auth.vo.TokenInfo;
import io.github.yanfeiwuji.isupabase.config.ISupabaseProperties;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/10 09:55
 */
@RestController
@RequestMapping(path = PgrstStrPool.AUTH_PATH)
@RequiredArgsConstructor
public class AuthAction {
    private final AuthService authService;
    private final UserMapper userMapper;
    private final OneTimeTokenService oneTimeTokenService;
    private final SessionService sessionService;
    private final JWTService jwtService;
    private final AuthRequestProvider authRequestProvider;
    private final ISupabaseProperties iSupabaseProperties;

    @PostMapping("/token")
    @Transactional
    public Object token(@RequestParam("grant_type") String grantType, @RequestBody TokenParam tokenParam) {

        switch (grantType) {
            case "password" -> {
                return authService.passwordLogin(tokenParam.getEmail(), tokenParam.getPassword());
            }
            case "refresh_token" -> {
                return authService.refreshToken(tokenParam.getRefreshToken());
            }
            case "client_credentials" -> {
                return "";
            }
            default -> {
                return null;
            }
        }
    }

    @GetMapping("/authorize")
    @Transactional
    public void authorize(@RequestParam(value = "provider") String provider,
                          @RequestParam(value = "redirect_to", required = false) String redirectTo,
                          HttpServletResponse response) throws IOException {
        final String needRedirectTo = ISupabasePropertiesUtils.redirectTo(redirectTo);
        final AuthRequest authRequest = Optional.ofNullable(iSupabaseProperties).map(ISupabaseProperties::getAuthProviders)
                .map(it -> it.get(provider))
                .flatMap(it -> authRequestProvider.apply(provider, it))
                .orElseThrow(AuthCmExFactory::unsupportedProvider);

        final String authorize = authRequest.authorize(jwtService.stateToken(provider, needRedirectTo));
        response.sendRedirect(authorize);
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/callback")
    public void callback(AuthCallback callback, HttpServletResponse response) throws IOException {

        final String state = callback.getState();
        final StateTokenInfo stateTokenInfo = jwtService.decodeStateToken(state);
        final String provider = stateTokenInfo.provider();
        final String referrer = stateTokenInfo.referrer();
        final AuthRequest authRequest = Optional.ofNullable(iSupabaseProperties).map(ISupabaseProperties::getAuthProviders)
                .map(it -> it.get(provider))
                .flatMap(it -> authRequestProvider.apply(provider, it))
                .orElseThrow(AuthCmExFactory::unsupportedProvider);
        final AuthResponse<AuthUser> authResponse;
        try {
            authResponse = authRequest.login(callback);
        } catch (Exception e) {

            response.sendRedirect(
                    AuthStrPool.ERROR_GETTING_USER_PROFILE_FROM_EXTERNAL_PROVIDER_URL_TEMP.formatted(referrer));
            return;
        }

        final AuthUser data = authResponse.getData();

        if (Objects.isNull(data) || CharSequenceUtil.isEmpty(data.getEmail())
                || !ValueValidUtils.isEmail(data.getEmail())) {
            response.sendRedirect(
                    AuthStrPool.ERROR_GETTING_USER_PROFILE_FROM_EXTERNAL_PROVIDER_URL_TEMP.formatted(referrer));
            return;
        }

        User user = authService.identityConfirmByEmail(provider, data);
        // gen token
        final TokenInfo<User> userTokenInfo = jwtService.userToTokenInfo(user);
        final AuthToken token = data.getToken();
        String redirectTo = JwtTokenUtils.oauthTokenToRedirectTo(userTokenInfo, referrer, token.getRefreshToken(),
                token.getAccessToken());
        response.sendRedirect(redirectTo);
    }

    @GetMapping("/verify")
    @Transactional
    public void verify(@RequestParam("token") String token,
                       @RequestParam("type") String type,
                       @RequestParam("redirect_to") String redirectTo,
                       HttpServletResponse response) throws IOException {

        Optional<OneTimeToken> oneTimeTokenOptional;

        switch (type) {
            case AuthStrPool.VERIFY_TYPE_SIGNUP ->
                    oneTimeTokenOptional = oneTimeTokenService.verifyToken(token, ETokenType.CONFIRMATION_TOKEN);
            case AuthStrPool.VERIFY_TYPE_RECOVERY ->
                    oneTimeTokenOptional = oneTimeTokenService.verifyToken(token, ETokenType.RECOVERY_TOKEN);
            case AuthStrPool.VERIFY_TYPE_EMAIL_CHANGE -> oneTimeTokenOptional = oneTimeTokenService.verifyToken(token);
            default -> oneTimeTokenOptional = Optional.empty();
        }

        String finalRedirectTo = ISupabasePropertiesUtils.redirectTo(redirectTo);

        final String needRedirect = oneTimeTokenOptional.map(oneTimeToken -> {
            switch (type) {
                case AuthStrPool.VERIFY_TYPE_SIGNUP -> {
                    authService.verifyConfirmationToken(oneTimeToken);
                    return finalRedirectTo;
                }
                case AuthStrPool.VERIFY_TYPE_RECOVERY -> {
                    return jwtService.oneTimeTokenToOTPTokenInfo(oneTimeToken)
                            .map(tokenInfo -> JwtTokenUtils.recoveryTokenToRedirectTo(tokenInfo, finalRedirectTo))
                            .orElseGet(() -> this.errorEmailLinkUrlString(finalRedirectTo));
                }
                case AuthStrPool.VERIFY_TYPE_EMAIL_CHANGE -> {
                    final Integer i = authService.verifyEmailChange(oneTimeToken);
                    switch (i) {
                        case 1 -> {
                            return AuthStrPool.EMAIL_CHANGE_FIRST_URL_TEMP.formatted(finalRedirectTo);
                        }
                        case 0 -> {
                            return jwtService.oneTimeTokenToOTPTokenInfo(oneTimeToken)
                                    .map(tokenInfo -> JwtTokenUtils.emailChangeTokenToRedirectTo(tokenInfo,
                                            finalRedirectTo))
                                    .orElseGet(() -> this.errorEmailLinkUrlString(finalRedirectTo));
                        }
                        case -1 -> {
                            return AuthStrPool.SERVER_ERROR_CONFIRM_EMAIL_TEMP.formatted(finalRedirectTo);
                        }
                        default -> {
                            return errorEmailLinkUrlString(finalRedirectTo);
                        }
                    }
                }
                default -> {
                    return finalRedirectTo;
                }
            }
        }).orElseGet(() -> this.errorEmailLinkUrlString(finalRedirectTo));

        response.sendRedirect(needRedirect);
    }

    @GetMapping("/user")
    @Transactional
    public User user() {
        return AuthUtils.uid().map(userMapper::selectOneWithRelationsById).orElseThrow();
    }

    @PutMapping("/user")
    @Transactional
    public User putUser(@RequestBody PutUserParam userParam,
                        @RequestParam(value = "redirect_to", required = false) String redirectTo) {
        userParam.setRedirectTo(redirectTo);
        return authService.putUser(userParam);
    }

    @PostMapping("/recover")
    @Transactional
    public Map<String, String> recover(@RequestBody RecoverParam recoverParam,
                                       @RequestParam(value = "redirect_to", required = false) String redirectTo) {

        recoverParam.setRedirectTo(redirectTo);
        authService.recover(recoverParam);
        return Map.of();
    }

    @PostMapping("signup")
    @Transactional
    public Object signUp(@RequestBody SignUpParam signUpParam,
                         @RequestParam(value = "redirect_to", required = false) String redirectTo) {
        Objects.requireNonNull(signUpParam);
        signUpParam.valid();
        signUpParam.setRedirectTo(redirectTo);
        if (CharSequenceUtil.isNotBlank(signUpParam.getPhone())) {
            authService.singUpByPhone("");
        } else {
            return authService.singUpByEmail(signUpParam);
        }
        return Map.of();
    }

    @PostMapping("logout")
    @Transactional
    public void logout(@RequestParam String scope) {
        // session id
        switch (scope) {
            case AuthStrPool.LOGOUT_SCOPE_GLOBAL, AuthStrPool.LOGOUT_SCOPE_OTHERS -> sessionService.logoutGlobal();
            case AuthStrPool.LOGOUT_SCOPE_LOCAL -> sessionService.logoutLocal();
            default -> throw AuthExFactory.NOT_IMPLEMENTED;
        }
    }

    private String errorEmailLinkUrlString(String redirectTo) {
        return AuthStrPool.ERROR_EMAIL_LINK_URL_TEMP.formatted(redirectTo);
    }

}
