package io.github.yanfeiwuji.isupabase.auth.listen;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import io.github.yanfeiwuji.isupabase.auth.action.param.RecoverParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.SignUpParam;
import io.github.yanfeiwuji.isupabase.auth.entity.ETokenType;
import io.github.yanfeiwuji.isupabase.auth.entity.OneTimeToken;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.event.RecoverEvent;
import io.github.yanfeiwuji.isupabase.auth.event.SignUpEvent;
import io.github.yanfeiwuji.isupabase.auth.mapper.OneTimeTokenMapper;
import io.github.yanfeiwuji.isupabase.auth.service.OneTimeTokenService;
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
    private final OneTimeTokenMapper oneTimeTokenMapper;
    private final OneTimeTokenService oneTimeTokenService;

    @EventListener(SignUpEvent.class)
    @Async
    public void onSignUp(SignUpEvent event) {
        final User user = event.getUser();
        final SignUpParam signUpParam = event.getSignUpParam();
        if (CharSequenceUtil.isNotEmpty(user.getEmail()) && Objects.isNull(user.getEmailConfirmedAt())) {
            putOneTimeToken(user);
            sendSingUpEmail(user, signUpParam);
        }


    }

    @EventListener(RecoverEvent.class)
    @Async
    public void onRecover(RecoverEvent event) {
        final User user = event.getUser();
        final OneTimeToken oneTimeToken = oneTimeTokenService.recoverToken(user);


        // todo add one time token
//        final SignUpParam signUpParam = event.getSignUpParam();
//        if (CharSequenceUtil.isNotEmpty(user.getEmail()) && Objects.isNull(user.getEmailConfirmedAt())) {
//            sendSingUpByEmail(user, signUpParam);
//        }
    }

    private void sendRecoverEmail(User user, RecoverParam recoverParam, OneTimeToken oneTimeToken) {
        final String redirectTo = Optional.ofNullable(recoverParam).map(RecoverParam::getRedirectTo)
                .orElseGet(ServletUtil::origin);
        //  https://vxguqjxsufwgzhcnojsc.supabase.co/auth/v1/verify?
        //  token=0f3186b9e85f51b12a9649c65f038c8a0c5c309df99d213380497139&type=recovery&redirect_to=http://localhost:5173/
        final MessageParam messageParam = MessageParam.of(siteUrl, user.getEmail(), oneTimeToken.getTokenHash(), redirectTo);

        messageParam.genRecoverConfirmationURL();
        final AuthMimeMessagePreparator authMimeMessagePreparator =
                mimeMessagePreparationFactory.ofResetPassword(user.getEmail(), messageParam);
        mailSender.send(authMimeMessagePreparator);

    }

    private void putOneTimeToken(User user) {
        final OneTimeToken oneTimeToken = new OneTimeToken();
        oneTimeToken.setUserId(user.getId());
        oneTimeToken.setTokenHash(user.getConfirmationToken());
        oneTimeToken.setTokenType(ETokenType.CONFIRMATION_TOKEN);
        oneTimeToken.setRelatesTo(user.getEmail());
        oneTimeTokenMapper.insert(oneTimeToken);
    }


    private void sendSingUpEmail(User user, SignUpParam signUpParam) {

        final String redirectTo = Optional.ofNullable(signUpParam).map(SignUpParam::getRedirectTo)
                .orElseGet(ServletUtil::origin);

        final MessageParam messageParam =
                MessageParam.of(siteUrl, user.getEmail(), user.getConfirmationToken(), redirectTo);

        messageParam.genSignUpConfirmationURL();
        final AuthMimeMessagePreparator authMimeMessagePreparator = mimeMessagePreparationFactory.ofSignup(user.getEmail(), messageParam);
        mailSender.send(authMimeMessagePreparator);
    }
}
