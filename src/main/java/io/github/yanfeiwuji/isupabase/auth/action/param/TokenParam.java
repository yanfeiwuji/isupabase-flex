package io.github.yanfeiwuji.isupabase.auth.action.param;

import lombok.Data;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 09:50
 */
@Data
public class TokenParam {
    private String email;
    private String password;
}
