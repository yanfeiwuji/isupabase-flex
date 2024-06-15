package io.github.yanfeiwuji.isupabase.auth.event;

import io.github.yanfeiwuji.isupabase.auth.action.param.SignUpParam;
import io.github.yanfeiwuji.isupabase.auth.entity.User;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 15:41
 */
@Getter
public class SignUpEvent extends UserEvent {
    private final transient SignUpParam signUpParam;

    public SignUpEvent(Object source, User user, SignUpParam signUpParam) {
        super(source, user);

        this.signUpParam = signUpParam;
    }
}
