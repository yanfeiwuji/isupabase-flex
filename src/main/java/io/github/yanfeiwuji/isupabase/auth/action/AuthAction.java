package io.github.yanfeiwuji.isupabase.auth.action;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import io.github.yanfeiwuji.isupabase.auth.action.param.RecoverParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.SignUpParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.TokenParam;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.mapper.UserMapper;
import io.github.yanfeiwuji.isupabase.auth.service.AuthService;
import io.github.yanfeiwuji.isupabase.auth.utils.AuthUtil;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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
    private final JavaMailSender mailSender;
    private final UserMapper userMapper;


    @PostMapping("/token")
    public Object token(@RequestParam("grant_type") String grantType, @RequestBody TokenParam tokenParam) {

        switch (grantType) {
            case "password" -> {
                return authService.passwordLogin(tokenParam.getEmail(), tokenParam.getPassword());
            }
            case "client_credentials" -> {
                return "";
            }
            default -> {
                return "123";
            }
        }
    }

    @GetMapping("/authorize")
    public Object authorize(@RequestParam("provider") String provider) {
        return provider;
    }

    @GetMapping("/user")
    public User user() {
        return AuthUtil.uid().map(userMapper::selectOneById).orElseThrow();
    }

    @PostMapping("/recover")
    public Map<String, String> recover(@RequestBody RecoverParam recoverParam) {

        authService.recover(recoverParam.getEmail());
        return Map.of();
    }

    @PostMapping("signup")
    public Object signUp(@RequestBody SignUpParam signUpParam) {
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
