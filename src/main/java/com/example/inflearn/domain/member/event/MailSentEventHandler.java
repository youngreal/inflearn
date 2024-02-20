package com.example.inflearn.domain.member.event;

import com.example.inflearn.common.exception.CustomMessagingException;
import com.example.inflearn.infra.mail.GmailService;
import com.example.inflearn.infra.mail.NaverMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailSentEventHandler {

    private final GmailService gmailService;
    private final NaverMailService naverMailService;

    @Async
    @Retryable(
            retryFor = CustomMessagingException.class,
            maxAttempts = 4,
            backoff = @Backoff(
                    delay = 1000,
                    maxDelay = 20000,
                    multiplier = 2.0,
                    random = true // jitter
            )
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 트랜잭션이 커밋된후에 이벤트가 실행된다.
    public void handle(MailSentEvent event) {
        gmailService.send(event.getMessage());
    }

    @Retryable(
            retryFor = CustomMessagingException.class,
            maxAttempts = 4,
            backoff = @Backoff(
                    delay = 1000,
                    maxDelay = 20000,
                    multiplier = 2.0,
                    random = true // jitter
            )
    )
    @Recover
    public void recoverMailSend(MailSentEvent event) {
        log.warn("recover start : retry 소진후 메일 전송실패");
        naverMailService.send(event.getMessage());
    }
}
