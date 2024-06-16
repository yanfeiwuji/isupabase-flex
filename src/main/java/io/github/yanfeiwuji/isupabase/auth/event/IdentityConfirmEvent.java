package io.github.yanfeiwuji.isupabase.auth.event;

import io.github.yanfeiwuji.isupabase.auth.entity.User;
import lombok.Getter;

import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/15 14:02
 */
@Getter
public abstract class IdentityConfirmEvent<T> extends UserEvent {
    private final String provider;
    private final transient T userInfo;
    private final transient Map<String, Object> identityData;

    protected IdentityConfirmEvent(Object source, User user, String provider, T userInfo) {
        super(source, user);
        this.provider = provider;
        this.userInfo = userInfo;

        this.identityData = userInfoToIdentityData(userInfo);
    }

    protected abstract Map<String, Object> userInfoToIdentityData(T userInfo);
}
