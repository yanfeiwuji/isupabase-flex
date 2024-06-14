package io.github.yanfeiwuji.isupabase.auth.action;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import io.github.yanfeiwuji.isupabase.auth.action.param.PutUserParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.RecoverParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.SignUpParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.TokenParam;
import io.github.yanfeiwuji.isupabase.auth.entity.ETokenType;
import io.github.yanfeiwuji.isupabase.auth.entity.OneTimeToken;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.ex.AuthEx;
import io.github.yanfeiwuji.isupabase.auth.ex.AuthExFactory;
import io.github.yanfeiwuji.isupabase.auth.ex.AuthExRes;
import io.github.yanfeiwuji.isupabase.auth.mapper.UserMapper;
import io.github.yanfeiwuji.isupabase.auth.service.AuthService;
import io.github.yanfeiwuji.isupabase.auth.service.JWTService;
import io.github.yanfeiwuji.isupabase.auth.service.OneTimeTokenService;
import io.github.yanfeiwuji.isupabase.auth.service.SessionService;
import io.github.yanfeiwuji.isupabase.auth.utils.AuthUtil;
import io.github.yanfeiwuji.isupabase.auth.vo.TokenInfo;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/10 09:55
 */
@RestController
@RequestMapping(path = PgrstStrPool.AUTH_RPC_PATH)
@AllArgsConstructor
public class AuthAction {
    private final AuthService authService;
    private final UserMapper userMapper;
    private final OneTimeTokenService oneTimeTokenService;
    private final SessionService sessionService;
    private final JWTService jwtService;


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
    public Object authorize(@RequestParam("provider") String provider) {
        return provider;
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

        final String needRedirect = oneTimeTokenOptional.map(oneTimeToken -> {
            switch (type) {
                case AuthStrPool.VERIFY_TYPE_SIGNUP -> {
                    authService.verifyConfirmationToken(oneTimeToken);
                    return redirectTo;
                }
                case AuthStrPool.VERIFY_TYPE_RECOVERY -> {
                    return jwtService.oneTimeTokenToOTPTokenInfo(oneTimeToken).map(tokenInfo -> AuthStrPool.RECOVERY_ACCESS_TOKEN_URL_TEMP.formatted(
                                    CharSequenceUtil.removeSuffix(redirectTo, StrPool.SLASH),
                                    tokenInfo.getAccessToken(),
                                    tokenInfo.getExpiresAt(),
                                    tokenInfo.getExpiresIn(),
                                    tokenInfo.getRefreshToken())
                            )
                            .orElseGet(() -> this.errorEmailLinkUrlString(redirectTo));
                }
                case AuthStrPool.VERIFY_TYPE_EMAIL_CHANGE -> {
                    final Integer i = authService.verifyEmailChange(oneTimeToken);
                    switch (i) {
                        case 1 -> {
                            return AuthStrPool.EMAIL_CHANGE_FIRST_URL.formatted(redirectTo);
                        }
                        case 0 -> {
                            return jwtService.oneTimeTokenToOTPTokenInfo(oneTimeToken).map(tokenInfo -> AuthStrPool.EMAIL_CHANGE_ACCESS_TOKEN_URL_TEMP.formatted(
                                            CharSequenceUtil.removeSuffix(redirectTo, StrPool.SLASH),
                                            tokenInfo.getAccessToken(),
                                            tokenInfo.getExpiresAt(),
                                            tokenInfo.getExpiresIn(),
                                            tokenInfo.getRefreshToken())
                                    )
                                    .orElseGet(() -> this.errorEmailLinkUrlString(redirectTo));
                        }
                        case -1 -> {
                            return AuthStrPool.SERVER_ERROR_CONFIRM_EMAIL_TEMP.formatted(redirectTo);
                        }
                        default -> {
                            return errorEmailLinkUrlString(redirectTo);
                        }
                    }
                }
                default -> {
                    return redirectTo;
                }
            }
        }).orElseGet(() -> this.errorEmailLinkUrlString(redirectTo));


        response.sendRedirect(needRedirect);
    }

    @GetMapping("/user")
    @Transactional
    public User user() {
        return AuthUtil.uid().map(userMapper::selectOneWithRelationsById).orElseThrow();
    }

    @PutMapping("/user")
    @Transactional
    public User putUser(@RequestBody PutUserParam userParam, @RequestParam(value = "redirect_to", required = false) String redirectTo) {
        userParam.setRedirectTo(redirectTo);
        return authService.putUser(userParam);
    }

    @PostMapping("/recover")
    @Transactional
    public Map<String, String> recover(@RequestBody RecoverParam recoverParam, @RequestParam(value = "redirect_to", required = false) String redirectTo) {

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
            default -> {
                throw AuthExFactory.NOT_IMPLEMENTED;
            }
        }
    }

    private String errorEmailLinkUrlString(String redirectTo) {
        return AuthStrPool.ERROR_EMAIL_LINK_URL_TEMP.formatted(CharSequenceUtil.removeSuffix(redirectTo, StrPool.SLASH));
    }


}
