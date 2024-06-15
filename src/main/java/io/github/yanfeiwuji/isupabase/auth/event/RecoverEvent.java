package io.github.yanfeiwuji.isupabase.auth.event;

import io.github.yanfeiwuji.isupabase.auth.action.param.RecoverParam;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 18:00
 */
@Getter
public class RecoverEvent extends ApplicationEvent {
    private final transient User user;
    private final transient RecoverParam recoverParam;

    public RecoverEvent(Object source, User user, RecoverParam recoverParam) {
        super(source);
        this.user = user;
        this.recoverParam = recoverParam;
    }
}
