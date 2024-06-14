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
import io.github.yanfeiwuji.isupabase.auth.ex.AuthExRes;
import io.github.yanfeiwuji.isupabase.auth.mapper.UserMapper;
import io.github.yanfeiwuji.isupabase.auth.service.AuthService;
import io.github.yanfeiwuji.isupabase.auth.service.OneTimeTokenService;
import io.github.yanfeiwuji.isupabase.auth.utils.AuthUtil;
import io.github.yanfeiwuji.isupabase.auth.vo.TokenInfo;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
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


    @PostMapping("/token")
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
    public Object authorize(@RequestParam("provider") String provider) {
        return provider;
    }

    @GetMapping("/verify")
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
            default -> oneTimeTokenOptional = Optional.empty();
        }

        final String needRedirect = oneTimeTokenOptional.map(oneTimeToken -> {
            switch (type) {
                case AuthStrPool.VERIFY_TYPE_SIGNUP -> {
                    authService.verifyConfirmationToken(oneTimeToken);
                    return redirectTo;
                }
                case AuthStrPool.VERIFY_TYPE_RECOVERY -> {
                    return authService.verifyRecovery(oneTimeToken).map(tokenInfo -> AuthStrPool.RECOVERY_ACCESS_TOKEN_URL_TEMP.formatted(
                                    CharSequenceUtil.removeSuffix(redirectTo, StrPool.SLASH),
                                    tokenInfo.getAccessToken(),
                                    tokenInfo.getExpiresAt(),
                                    tokenInfo.getExpiresIn(),
                                    tokenInfo.getRefreshToken())
                            )
                            .orElseGet(() -> this.errorEmailLinkUrlString(redirectTo));
                }
                default -> {
                    return redirectTo;
                }
            }
        }).orElseGet(() -> this.errorEmailLinkUrlString(redirectTo));

        response.sendRedirect(needRedirect);


    }

    @GetMapping("/user")
    public User user() {
        return AuthUtil.uid().map(userMapper::selectOneById).orElseThrow();
    }

    @PutMapping("/user")
    public User putUser(@RequestBody PutUserParam userParam) {
        // todo edit user
        return AuthUtil.uid().map(userMapper::selectOneById).orElseThrow();
    }

    @PostMapping("/recover")
    public Map<String, String> recover(@RequestBody RecoverParam recoverParam, @RequestParam(value = "redirect_to", required = false) String redirectTo) {

        recoverParam.setRedirectTo(redirectTo);
        authService.recover(recoverParam);
        return Map.of();
    }

    @PostMapping("signup")
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
    public void logout(@RequestParam String scope) {
        // session id
        switch (scope) {
            case AuthStrPool.LOGOUT_SCOPE_GLOBAL, AuthStrPool.LOGOUT_SCOPE_OTHERS -> authService.logoutGlobal();
            case AuthStrPool.LOGOUT_SCOPE_LOCAL -> authService.logoutLocal();
            default -> {
            }
        }
    }

    private String errorEmailLinkUrlString(String redirectTo) {

        return AuthStrPool.ERROR_EMAIL_LINK_URL_TEMP.formatted(CharSequenceUtil.removeSuffix(redirectTo, StrPool.SLASH));
    }


}
