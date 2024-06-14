package io.github.yanfeiwuji.isupabase.auth.event;

import io.github.yanfeiwuji.isupabase.auth.entity.User;
import lombok.Getter;

/**
 * @author yanfeiwuji
 * @date 2024/6/14 21:21
 */
@Getter
public class EmailVerifiedEvent extends UserEvent {

    public EmailVerifiedEvent(Object source, User user) {
        super(source, user);
    }
}
