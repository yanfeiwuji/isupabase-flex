package io.github.yanfeiwuji.isupabase.flex;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 13:59
 */
@Data
@AllArgsConstructor
public class SimpleAuthContext implements AuthContext {
    private Long uid;
    private String role;

}
