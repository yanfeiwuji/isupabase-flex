package io.github.yanfeiwuji.isupabase.auth.service.email;

import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 11:14
 */
public record AuthMimeMessagePreparator(
        String personal,
        String from,
        String to,
        String subject,
        String text) implements MimeMessagePreparator {
    @Override
    public void prepare(MimeMessage mimeMessage) throws Exception {
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        final InternetAddress internetAddress = new InternetAddress(from);
        internetAddress.setPersonal(personal);
        mimeMessage.setFrom(internetAddress);
        mimeMessage.setSubject(subject);
        mimeMessage.setContent(text, AuthStrPool.EMAIL_CONTENT_TYPE);
    }
}
