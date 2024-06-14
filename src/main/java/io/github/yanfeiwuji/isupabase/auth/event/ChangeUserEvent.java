package io.github.yanfeiwuji.isupabase.auth.event;

import io.github.yanfeiwuji.isupabase.auth.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author yanfeiwuji
 * @date 2024/6/14 13:55
 */
@Getter
public abstract class ChangeUserEvent extends ApplicationEvent {
    private final User user;

    protected ChangeUserEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}