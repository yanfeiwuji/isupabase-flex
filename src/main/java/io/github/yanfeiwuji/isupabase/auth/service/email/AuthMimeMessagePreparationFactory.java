package io.github.yanfeiwuji.isupabase.auth.service.email;

import org.springframework.beans.factory.annotation.Value;

import java.text.MessageFormat;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 11:35
 */

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


}
