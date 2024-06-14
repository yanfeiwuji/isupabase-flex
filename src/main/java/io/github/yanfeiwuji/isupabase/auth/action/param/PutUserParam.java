package io.github.yanfeiwuji.isupabase.auth.action.param;

import lombok.Data;

import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/14 11:33
 */
@Data
public class PutUserParam {
    private String email;
    private String phone;
    private String password;
    private String nonce;
    private Map<String, Object> data;
}
