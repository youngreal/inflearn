package com.example.musinsa.event;

import com.example.musinsa.domain.member.domain.Member;
import com.example.musinsa.infra.mail.EmailMessage;
import lombok.Getter;

@Getter
public class MailSentEvent{

    private final EmailMessage message;

    public MailSentEvent(Member member) {
        this.message = emailMessage(member);
    }
}
