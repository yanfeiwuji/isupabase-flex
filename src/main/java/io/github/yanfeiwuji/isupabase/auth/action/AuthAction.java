package io.github.yanfeiwuji.isupabase.auth.action;

import io.github.yanfeiwuji.isupabase.auth.service.AuthService;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * @author yanfeiwuji
 * @date 2024/6/10 09:55
 */
@RestController
@RequestMapping(path = PgrstStrPool.AUTH_RPC_PATH)
@AllArgsConstructor
public class AuthAction {
    private final AuthService authService;

    @PostMapping("/token")
    public Object token(@RequestParam("grant_type") String grantType) {
        switch (grantType) {
            case "password" -> {
                return authService.passwordLogin("", "");
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

}
