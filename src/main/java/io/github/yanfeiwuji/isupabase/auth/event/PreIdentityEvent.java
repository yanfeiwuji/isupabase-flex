package io.github.yanfeiwuji.isupabase.auth.event;


import io.github.yanfeiwuji.isupabase.auth.entity.User;
import lombok.Getter;

/**
 * @author yanfeiwuji
 * @date 2024/6/14 17:08
 */
@Getter
public class PreIdentityEvent extends UserEvent {

    public final String provider;

    public PreIdentityEvent(Object source, User user, String provider) {
        super(source, user);
        this.provider = provider;
    }
}
