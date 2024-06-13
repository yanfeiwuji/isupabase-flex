package io.github.yanfeiwuji.isupabase.auth.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * @author yanfeiwuji
 * @date 2024/6/12 11:35
 */
@Service
public class AuthMimeMessagePreparationFactory {
    @Value("${spring.mail.username}")
    private String fromEmail;


    public AuthMimeMessagePreparator ofSignup(String email, MessageParam messageParam) {
        // only simple info
        return new AuthMimeMessagePreparator("noreply", fromEmail, email, "Confirm Your Signup", """
                <h2>Confirm your signup</h2>
                <p>Follow this link to confirm your user:</p>
                <p><a href="%s">Confirm your mail</a></p>
                """
                .formatted(messageParam.getConfirmationURL()));
    }


    public AuthMimeMessagePreparator ofResetPassword(String email, MessageParam messageParam) {
        // only simple info
        return new AuthMimeMessagePreparator("noreply", fromEmail, email, "Confirm Your Signup", """
                <h2>Reset Password</h2>
                <p>Follow this link to reset the password for your user:</p>
                <p><a href="%s">Reset Password</a></p>
                """
                .formatted(messageParam.getConfirmationURL()));
    }


}
