package com.example.musinsa.infra.mail;

import org.springframework.stereotype.Component;

public interface MailService {

    void send(EmailMessage emailMessage);
}
