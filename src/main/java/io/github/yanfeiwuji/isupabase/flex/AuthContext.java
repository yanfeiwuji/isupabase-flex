package io.github.yanfeiwuji.isupabase.flex;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 09:39
 */
// todo add ext info to jwt and from jwt get default context
public interface AuthContext {
    Long getUid();

    String getRole();

    default boolean isAnon() {
        return CharSequenceUtil.equals(getRole(), AuthStrPool.ANON_ROLE);
    }

    default boolean isAuth() {
        return CharSequenceUtil.equals(getRole(), AuthStrPool.AUTHENTICATED_ROLE);
    }

    default boolean isServiceRole() {
        return CharSequenceUtil.equals(getRole(), AuthStrPool.SERVICE_ROLE);
    }

}
