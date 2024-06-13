package io.github.yanfeiwuji.isupabase.auth.listen;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import io.github.yanfeiwuji.isupabase.auth.action.param.SignUpParam;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.event.SignUpEvent;
import io.github.yanfeiwuji.isupabase.auth.service.email.AuthMimeMessagePreparationFactory;
import io.github.yanfeiwuji.isupabase.auth.service.email.AuthMimeMessagePreparator;
import io.github.yanfeiwuji.isupabase.auth.service.email.MessageParam;
import io.github.yanfeiwuji.isupabase.auth.utils.ServletUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 15:43
 */
@Service
@RequiredArgsConstructor
public class UserListen {
    @Value("${isupabase.site-url}")
    private String siteUrl;

    private final JavaMailSender mailSender;

    private final AuthMimeMessagePreparationFactory mimeMessagePreparationFactory;

    @EventListener(SignUpEvent.class)
    @Async
    public void onCreateUser(SignUpEvent event) {
        final User user = event.getUser();
        final SignUpParam signUpParam = event.getSignUpParam();
        if (CharSequenceUtil.isNotEmpty(user.getEmail()) && Objects.isNull(user.getEmailConfirmedAt())) {
            sendSingUpByEmail(user, signUpParam);
        }


    }

    private void sendSingUpByEmail(User user, SignUpParam signUpParam) {

        final String redirectTo = Optional.ofNullable(signUpParam).map(SignUpParam::getRedirectTo)
                .orElseGet(ServletUtil::origin);

        final MessageParam messageParam = MessageParam.builder()
                .email(user.getEmail())
                .newEmail(null)
                .siteURL(siteUrl)
                .token(user.getConfirmationToken())
                .redirectTo(redirectTo)

                .build();
        messageParam.genSignUpConfirmationURL();
        final AuthMimeMessagePreparator authMimeMessagePreparator =
                mimeMessagePreparationFactory.ofSignup(user.getEmail(), messageParam);
        mailSender.send(authMimeMessagePreparator);
    }
}
