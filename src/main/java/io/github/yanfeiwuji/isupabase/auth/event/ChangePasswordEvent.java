package io.github.yanfeiwuji.isupabase.auth.event;

import io.github.yanfeiwuji.isupabase.auth.entity.User;

/**
 * @author yanfeiwuji
 * @date 2024/6/14 13:55
 */

public class ChangePasswordEvent extends ChangeUserEvent {
    public ChangePasswordEvent(Object source, User user) {
        super(source, user);
    }
}
