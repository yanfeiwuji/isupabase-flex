package io.github.yanfeiwuji.isupabase.auth.listen;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.yanfeiwuji.isupabase.auth.action.param.RecoverParam;
import io.github.yanfeiwuji.isupabase.auth.action.param.SignUpParam;
import io.github.yanfeiwuji.isupabase.auth.entity.ETokenType;
import io.github.yanfeiwuji.isupabase.auth.entity.OneTimeToken;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.event.*;
import io.github.yanfeiwuji.isupabase.auth.mapper.OneTimeTokenMapper;
import io.github.yanfeiwuji.isupabase.auth.mapper.UserMapper;
import io.github.yanfeiwuji.isupabase.auth.provider.AuthMimeMessagePreparationProvider;
import io.github.yanfeiwuji.isupabase.auth.service.IdentityService;
import io.github.yanfeiwuji.isupabase.auth.service.OneTimeTokenService;
import io.github.yanfeiwuji.isupabase.auth.service.SessionService;
import io.github.yanfeiwuji.isupabase.auth.service.email.AuthMimeMessagePreparator;
import io.github.yanfeiwuji.isupabase.auth.service.email.MessageParam;
import io.github.yanfeiwuji.isupabase.auth.utils.ServletUtils;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
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

    private final AuthMimeMessagePreparationProvider mimeMessagePreparationFactory;
    private final OneTimeTokenMapper oneTimeTokenMapper;
    private final OneTimeTokenService oneTimeTokenService;
    private final SessionService sessionService;
    private final UserMapper userMapper;
    private final IdentityService identityService;

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
        final RecoverParam recoverParam = event.getRecoverParam();
        final OneTimeToken oneTimeToken = oneTimeTokenService.recoverToken(user);
        sendRecoverEmail(user, recoverParam, oneTimeToken);
    }

    @EventListener(ChangeEmailEvent.class)
    @Async
    public void onChangeEmail(ChangeEmailEvent event) {
        final User user = event.getUser();
        // change email
        final OneTimeToken emailChangeTokenCurrent = oneTimeTokenService.emailChangeTokenCurrent(user);
        final OneTimeToken emailChangeTokenNew = oneTimeTokenService.emailChangeTokenNew(user);

        sendEmailChangeTokenCurrent(user, emailChangeTokenCurrent, event.getRedirectTo());
        sendEmailChangeTokenNew(user, emailChangeTokenNew, event.getRedirectTo());
    }

    @EventListener(ChangePasswordEvent.class)
    public void onChangePassword(ChangePasswordEvent event) {
        sessionService.logoutGlobal();
    }


    // identityConfirm
    @EventListener(OauthIdentityConfirmEvent.class)
    public void onOauthIdentityConfirm(OauthIdentityConfirmEvent event) {
        final User user = event.getUser();
        final String provider = event.getProvider();
        final AuthUser userInfo = event.getUserInfo();
        final Map<String, Object> identityData = event.getIdentityData();

        identityService.identityConfirm(user, provider, userInfo.getUuid(), identityData);
    }

    @EventListener(FetchTokenEvent.class)
    public void onFetchToken(FetchTokenEvent event) {
        final User user = event.getUser();
        user.setLastSignInAt(OffsetDateTime.now());
        userMapper.update(user);
    }

    @EventListener(EmailVerifiedEvent.class)
    public void onEmailVerified(EmailVerifiedEvent event) {
        final User user = event.getUser();
        //  use one time token then user’s  email confirmed
        if (Objects.isNull(user.getEmailConfirmedAt())) {
            user.setEmailConfirmedAt(OffsetDateTime.now());
            userMapper.update(user);
        }
        identityService.identityConfirm(user, AuthStrPool.IDENTITY_PROVIDER_EMAIL, String.valueOf(user.getId()), new HashMap<>());

    }

    private void sendEmailChangeTokenCurrent(User user, OneTimeToken oneTimeToken, String redirectTo) {
        final String needRedirectTo = Optional.ofNullable(redirectTo).orElseGet(ServletUtils::origin);
        final MessageParam messageParam = MessageParam.of(siteUrl, user.getEmail(), user.getEmailChange(), oneTimeToken.getTokenHash(), needRedirectTo);
        messageParam.genEmailChangeConfirmationURL();

        final AuthMimeMessagePreparator authMimeMessagePreparator =
                mimeMessagePreparationFactory.ofEmailChange(user.getEmail(), messageParam);
        mailSender.send(authMimeMessagePreparator);
    }

    private void sendEmailChangeTokenNew(User user, OneTimeToken oneTimeToken, String redirectTo) {

        final String needRedirectTo = Optional.ofNullable(redirectTo).orElseGet(ServletUtils::origin);
        final MessageParam messageParam = MessageParam.of(siteUrl, user.getEmail(), user.getEmailChange(), oneTimeToken.getTokenHash(), needRedirectTo);
        messageParam.genEmailChangeConfirmationURL();

        final AuthMimeMessagePreparator authMimeMessagePreparator =
                mimeMessagePreparationFactory.ofEmailChange(user.getEmailChange(), messageParam);
        mailSender.send(authMimeMessagePreparator);
    }


    private void sendRecoverEmail(User user, RecoverParam recoverParam, OneTimeToken oneTimeToken) {
        final String redirectTo = Optional.ofNullable(recoverParam).map(RecoverParam::getRedirectTo)
                .orElseGet(ServletUtils::origin);

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
                .orElseGet(ServletUtils::origin);

        final MessageParam messageParam =
                MessageParam.of(siteUrl, user.getEmail(), user.getConfirmationToken(), redirectTo);

        messageParam.genSignUpConfirmationURL();
        final AuthMimeMessagePreparator authMimeMessagePreparator = mimeMessagePreparationFactory.ofSignup(user.getEmail(), messageParam);
        mailSender.send(authMimeMessagePreparator);
    }
}
