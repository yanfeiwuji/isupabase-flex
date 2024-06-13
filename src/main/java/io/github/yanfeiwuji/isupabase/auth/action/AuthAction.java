package io.github.yanfeiwuji.isupabase.auth.action;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import io.github.yanfeiwuji.isupabase.auth.action.param.RecoverParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.SignUpParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.TokenParam;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.ex.AuthExRes;
import io.github.yanfeiwuji.isupabase.auth.mapper.UserMapper;
import io.github.yanfeiwuji.isupabase.auth.service.AuthService;
import io.github.yanfeiwuji.isupabase.auth.utils.AuthUtil;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

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
        AuthExRes authExRes;
        switch (type) {
            case AuthStrPool.VERIFY_TYPE_SIGNUP -> authExRes = authService.verifySignUp(token);
            // todo
            case AuthStrPool.VERIFY_TYPE_RECOVERY -> authExRes = authService.verifyRecovery(token);
            default -> authExRes = AuthExRes.EMAIL_LINK_ERROR;
        }

        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(redirectTo);
        if (Objects.nonNull(authExRes)) {
            uriComponentsBuilder
                    .queryParam(AuthStrPool.QUERY_PARAM_ERROR, authExRes.error())
                    .queryParam(AuthStrPool.QUERY_PARAM_ERROR_CODE, AuthStrPool.QUERY_PARAM_ERROR_CODE_DEFAULT_VALUE)
                    .queryParam(AuthStrPool.QUERY_PARAM_ERROR_DESCRIPTION, authExRes.errorDescription());
        }
        final String uriString = uriComponentsBuilder.build().toUriString();
        response.sendRedirect(uriString);
    }

    @GetMapping("/user")
    public User user() {
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
}
