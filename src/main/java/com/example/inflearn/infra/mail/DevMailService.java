package com.example.inflearn.infra.mail;

import com.example.inflearn.common.exception.CustomMessagingException;
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

    /**
     * MessagingException 발생시 로그를 남기기위해 catch를 하게되면 MailSentEventHandler에서 MessagingException에 대한 트리거가 작동되지않음.
     * 따라서, 로그도 남겨주면서 커스텀예외로 전환해서 CustomMessagingException을 retry의 트리거로 동작시킨다.
     */
    @Override
    public void send(EmailMessage emailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            log.info("sent email: {}", emailMessage.message());
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            setMessage(emailMessage, mimeMessageHelper);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.warn("messaging exception 발생, retry 시작");
            throw new CustomMessagingException(e);
        }
    }

    private void setMessage(EmailMessage emailMessage, MimeMessageHelper mimeMessageHelper)
            throws MessagingException {
        mimeMessageHelper.setTo(emailMessage.to());
        mimeMessageHelper.setSubject(emailMessage.subject());
        mimeMessageHelper.setText(emailMessage.message(), true);
    }
}
