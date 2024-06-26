package io.github.yanfeiwuji.isupabase.auth.provider;

import io.github.yanfeiwuji.isupabase.auth.service.email.AuthMimeMessagePreparator;
import io.github.yanfeiwuji.isupabase.auth.service.email.MessageParam;
import lombok.RequiredArgsConstructor;


/**
 * @author yanfeiwuji
 * @date 2024/6/12 11:35
 */
@RequiredArgsConstructor
public class DefaultAuthMimeMessagePreparationProvider implements AuthMimeMessagePreparationProvider {

    private final String fromEmail;

    @Override
    public AuthMimeMessagePreparator ofSignup(String email, MessageParam messageParam) {
        // only simple info
        return new AuthMimeMessagePreparator("noreply", fromEmail, email, "Confirm Your Signup", """
                <h2>Confirm your signup</h2>
                <p>Follow this link to confirm your user:</p>
                <p><a href="%s">Confirm your mail</a></p>
                """
                .formatted(messageParam.getConfirmationURL()));
    }

    @Override
    public AuthMimeMessagePreparator ofResetPassword(String email, MessageParam messageParam) {
        // only simple info
        return new AuthMimeMessagePreparator("noreply", fromEmail, email, "Reset Your Password", """
                <h2>Reset Password</h2>
                <p>Follow this link to reset the password for your user:</p>
                <p><a href="%s">Reset Password</a></p>
                """
                .formatted(messageParam.getConfirmationURL()));
    }

    @Override
    public AuthMimeMessagePreparator ofEmailChange(String email, MessageParam messageParam) {
        // only simple info
        return new AuthMimeMessagePreparator("noreply", fromEmail, email, "Confirm Email Change", """
                <h2>Confirm Change of Email</h2>
                <p>Follow this link to confirm the update of your email from %s to %s:</p>
                <p><a href="%s">Change Email</a></p>
                """
                .formatted(messageParam.getEmail(), messageParam.getNewEmail(), messageParam.getConfirmationURL()));
    }


}
