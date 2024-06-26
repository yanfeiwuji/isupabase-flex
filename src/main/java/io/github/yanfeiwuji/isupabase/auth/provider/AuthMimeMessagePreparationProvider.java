package io.github.yanfeiwuji.isupabase.auth.provider;

import io.github.yanfeiwuji.isupabase.auth.service.email.AuthMimeMessagePreparator;
import io.github.yanfeiwuji.isupabase.auth.service.email.MessageParam;

/**
 * @author yanfeiwuji
 * @date 2024/6/26 16:39
 */
public interface AuthMimeMessagePreparationProvider {
    AuthMimeMessagePreparator ofSignup(String email, MessageParam messageParam);

    AuthMimeMessagePreparator ofResetPassword(String email, MessageParam messageParam);

    AuthMimeMessagePreparator ofEmailChange(String email, MessageParam messageParam);
}
