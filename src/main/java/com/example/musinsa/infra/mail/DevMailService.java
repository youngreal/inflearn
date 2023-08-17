package com.example.musinsa.infra.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Profile("dev")
@Component
public class DevMailService implements MailService{

    private final JavaMailSender javaMailSender;

    @Override
    public void send(EmailMessage emailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false,
                    "UTF-8");
            mimeMessageHelper.setTo(emailMessage.to());
            mimeMessageHelper.setSubject(emailMessage.subject());
            mimeMessageHelper.setText(emailMessage.message(), true);
            javaMailSender.send(mimeMessage);
            log.info("sent email: {}", emailMessage.message());
        } catch (MessagingException e) {
            log.error("failed to send email", e);
            throw new RuntimeException(e);
        }
    }
}
