package io.github.yanfeiwuji.isupabase.auth.event;

import io.github.yanfeiwuji.isupabase.auth.entity.User;

import lombok.Getter;

/**
 * @author yanfeiwuji
 * @date 2024/6/14 13:43
 */
@Getter
public class ChangeEmailEvent extends UserEvent {
    private final String redirectTo;

    public ChangeEmailEvent(Object source, User user, String redirectTo) {
        super(source, user);
        this.redirectTo = redirectTo;
    }
}
