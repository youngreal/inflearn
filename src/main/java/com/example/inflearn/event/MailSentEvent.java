package com.example.inflearn.event;

import com.example.inflearn.domain.member.domain.Member;
import com.example.inflearn.infra.mail.EmailMessage;
import lombok.Getter;

@Getter
public class MailSentEvent{

    private final EmailMessage message;

    public MailSentEvent(Member member) {
        this.message = emailMessage(member);
    }

    private EmailMessage emailMessage(Member member) {
        return EmailMessage.builder()
                .to(member.getEmail())
                .subject("[인프런] 회원가입을 위해 메일인증을 해주세요.")
                .message("안녕하세요, 인프랩입니다. 아래 메일 인증 버튼을 눌러 회원가입을 완료해주세요.\n"
                        + "/check-email-token?emailToken=" + member.getEmailToken() +
                        "&email=" + member.getEmail())
                .build();
    }
}
