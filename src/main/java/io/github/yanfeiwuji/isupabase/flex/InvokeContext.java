package io.github.yanfeiwuji.isupabase.flex;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/16 11:50
 */
@Data
public class InvokeContext {
    private Long uid;
    private String role;

    private Map<String, Object> ext;

    public InvokeContext(Long uid, String role) {
        this.uid = uid;
        this.role = role;
        this.ext = new HashMap<>();
    }

    public boolean isAnon() {
        return CharSequenceUtil.equals(role, AuthStrPool.ANON_ROLE);
    }

    public boolean isAuth() {
        return CharSequenceUtil.equals(role, AuthStrPool.AUTHENTICATED_ROLE);
    }

    public boolean isServiceRole() {
        return CharSequenceUtil.equals(role, AuthStrPool.SERVICE_ROLE);
    }


}
