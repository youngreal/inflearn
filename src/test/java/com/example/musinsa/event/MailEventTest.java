package com.example.musinsa.event;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.musinsa.common.exception.CustomMessagingException;
import com.example.musinsa.domain.member.domain.Member;
import com.example.musinsa.domain.member.service.MemberService;
import com.example.musinsa.infra.mail.MailService;
import com.example.musinsa.infra.repository.member.MemberRepository;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class MailEventTest {

    private static final int TIMEOUT_FOR_TEST = 30;

    @Autowired
    private MailSentEventHandler sut;

    @Autowired
    private MemberService memberService;

    @MockBean
    private MailService mailService;

    @MockBean
    private MemberRepository memberRepository;

    private MailSentEvent event;
    private Member member;

    @BeforeEach
    void setup() {
        member = Member.builder()
                .email("asdf1234@naver.com")
                .password("12345678!!")
                .build();

        event = new MailSentEvent(member);
    }

    @DisplayName("회원가입시 예외가 발생하면 메일이 전송되지않는다")
    @Test
    void test0() {
        // given
        given(memberRepository.existsByEmail(member.getEmail())).willReturn(false);
        given(memberRepository.save(any())).willThrow(new RuntimeException());

        // when
        try {
            memberService.signUp(member);
        } catch (RuntimeException e) {
            System.out.println("테스트 통과를 위해 예외 catch");
        }

        // then
        await().untilAsserted(() -> then(mailService).shouldHaveNoInteractions());
    }

    @DisplayName("메일전송 이벤트발생시 성공적으로 메일이 전송된다")
    @Test
    void test() {
        // when
        sut.handle(event);

        // then
        await().untilAsserted(() -> verify(mailService).send(any()));
    }

    @DisplayName("메일서버에서 예외발생시 retry가 최대 5회 동작한다")
    @Test
    void retry() {
        // given
        doThrow(CustomMessagingException.class)
                .when(mailService)
                .send(any());

        // when
        sut.handle(event);

        // then
        // 조건이 true가 되거나 시간초과에 도달할때까지 기다리며 테스트에서 비동기작업을 처리/확인방법을 제공
        await().timeout(TIMEOUT_FOR_TEST, TimeUnit.SECONDS).untilAsserted(() -> verify(mailService, times(5)).send(any()));
    }
}