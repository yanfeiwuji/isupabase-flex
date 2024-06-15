package io.github.yanfeiwuji.isupabase.auth.event;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import me.zhyd.oauth.model.AuthUser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/15 17:24
 */

public class OauthIdentityConfirmEvent extends IdentityConfirmEvent<AuthUser> {
    private static final CopyOptions COPY_OPTIONS_IGNORE_MORE = CopyOptions.create().setIgnoreProperties("token", "rawUserInfo");

    public OauthIdentityConfirmEvent(Object source, User user, String provider, AuthUser userInfo) {
        super(source, user, provider, userInfo);

    }

    @Override
    protected Map<String, Object> userInfoToIdentityData(AuthUser userInfo) {
        return BeanUtil.beanToMap(userInfo, new HashMap<>(), COPY_OPTIONS_IGNORE_MORE);
    }
}
