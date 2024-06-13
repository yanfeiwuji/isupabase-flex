package io.github.yanfeiwuji.isupabase.auth.ex;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 11:52
 */
@Getter
@AllArgsConstructor
public class AuthCmEx extends RuntimeException {
    private final AuthCmExRes authCmExRes;
}
