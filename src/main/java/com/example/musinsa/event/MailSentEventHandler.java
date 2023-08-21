package com.example.musinsa.event;

import com.example.musinsa.infra.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MailSentEventHandler {
    private final MailService mailService;

    @EventListener(MailSentEvent.class)
    public void handle(MailSentEvent event) {
        mailService.send(event.getMessage());
    }
}
