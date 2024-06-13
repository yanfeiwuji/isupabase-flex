package io.github.yanfeiwuji.isupabase.auth.ex;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 10:46
 */

@AllArgsConstructor
@Getter
public class AuthEx extends RuntimeException {
    private final AuthExRes authExRes;
}
