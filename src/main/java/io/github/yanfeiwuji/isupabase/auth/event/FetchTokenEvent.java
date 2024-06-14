package io.github.yanfeiwuji.isupabase.auth.event;

import io.github.yanfeiwuji.isupabase.auth.entity.User;
import lombok.Getter;

/**
 * @author yanfeiwuji
 * @date 2024/6/14 16:44
 */

@Getter
public class FetchTokenEvent extends UserEvent {

    public FetchTokenEvent(Object source, User user) {
        super(source, user);
    }
}
