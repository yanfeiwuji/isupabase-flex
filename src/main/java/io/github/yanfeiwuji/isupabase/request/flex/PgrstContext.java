package io.github.yanfeiwuji.isupabase.request.flex;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/19 09:32
 */
@Data
@AllArgsConstructor
public class PgrstContext {
    private Long uid;
    private String role;
    private Optional<Jwt> jwt;

    public boolean isAnon() {
        return CharSequenceUtil.equals(getRole(), AuthStrPool.ANON_ROLE);
    }

    public boolean isAuth() {
        return CharSequenceUtil.equals(getRole(), AuthStrPool.AUTHENTICATED_ROLE);
    }

    public boolean isServiceRole() {
        return CharSequenceUtil.equals(getRole(), AuthStrPool.SERVICE_ROLE);
    }

}
