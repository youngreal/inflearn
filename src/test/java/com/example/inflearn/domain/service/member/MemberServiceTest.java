package com.example.inflearn.domain.service.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.inflearn.common.exception.AlreadyExistMemberException;
import com.example.inflearn.common.exception.DoesNotExistEmailException;
import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.WrongEmailTokenException;
import com.example.inflearn.domain.member.domain.Member;
import com.example.inflearn.domain.member.service.MemberService;
import com.example.inflearn.domain.member.event.Events;
import com.example.inflearn.domain.member.event.MailSentEvent;
import com.example.inflearn.infra.mail.EmailMessage;
import com.example.inflearn.infra.mail.MailService;
import com.example.inflearn.infra.repository.member.MemberRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService sut;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MailService mailService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<MailSentEvent> mailSentEventCaptor;

    @Test
    void 회원가입_성공() {
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678!!")
                .build();

        // given
        given(memberRepository.existsByEmail(member.getEmail())).willReturn(false);
        assertThat(member.getEmailToken()).isNull();
        Events.setPublisher(eventPublisher);

        // when
        sut.signUp(member);

        // then
        assertThat(member.getEmailToken()).isNotNull();
        then(eventPublisher).should().publishEvent(mailSentEventCaptor.capture());
        then(memberRepository).should().save(any(Member.class));
    }

    @Test
    void 회원가입_실패_이미_존재하는_유저() {
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678!!")
                .build();

        // given
        given(memberRepository.existsByEmail(member.getEmail())).willReturn(true);

        // when & then
        assertThrows(AlreadyExistMemberException.class, () -> sut.signUp(member));
    }

    @Test
    void 메일_체크_성공시_member의_isVerify필드_상태를_변경한다() {
        // given
        Member member = Member.builder()
                .email("asdf1234@naver.com")
                .emailToken(UUID.randomUUID().toString())
                .isVerifiedEmail(false)
                .password("12345678!!")
                .build();
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));

        //when
        sut.checkEmail(member.getEmailToken(), member.getEmail());

        // then
        assertThat(member.isVerifiedEmail()).isTrue();
    }

    @Test
    void 메일_체크_실패_존재하지_않는_이메일() {
        // given
        Member member = Member.builder()
                .email("asdf1234@naver.com")
                .emailToken(UUID.randomUUID().toString())
                .password("12345678!!")
                .build();
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistEmailException.class, () -> sut.checkEmail(member.getEmailToken(), member.getEmail()));
    }

    @Test
    void 메일_체크_실패_유효하지_않은_이메일과_토큰번호() {
        // given
        Member member = Member.builder()
                .email("asdf1234@naver.com")
                .emailToken(null)
                .password("12345678!!")
                .build();
        given(memberRepository.findByEmail(member.getEmail())).willReturn(
                Optional.of(Member.builder()
                        .email("asdf1234@naver.com")
                        .emailToken(UUID.randomUUID().toString())
                        .password("12345678!!")
                        .build()));

        // when & then
        assertThrows(WrongEmailTokenException.class,
                () -> sut.checkEmail(member.getEmailToken(), member.getEmail()));
    }

    @Test
    void 로그인_성공_후_토큰을_발급한다() {
        // given
        Member member = Member.builder()
                .email("asdf1234@naver.com")
                .password("12345678!!")
                .loginToken(null)
                .isVerifiedEmail(true)
                .build();

        given(memberRepository.findByEmailAndPassword(member.getEmail(),
                member.getPassword())).willReturn(Optional.of(member));

        // when
        String loginToken = sut.login(member);

        // then
        assertThat(loginToken).isNotNull();
    }

    @Test
    void 로그인_실패_존재하지_않는_유저() {
        // given
        Member member = Member.builder()
                .email("asdf1234@naver.com")
                .password("12345678!!")
                .loginToken(null)
                .build();

        given(memberRepository.findByEmailAndPassword(member.getEmail(),
                member.getPassword())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.login(member));
    }

    @Test
    void 로그아웃_후_토큰은_null이된다() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678!!")
                .loginToken(UUID.randomUUID().toString())
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));

        // when
        sut.logout(member.getId());

        // then
        assertThat(member.getLoginToken()).isNull();
    }

    @Test
    void 로그아웃_실패_존재하지_않는_유저() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678!!")
                .loginToken("UUID-12345678")
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.logout(member.getId()));
        assertThat(member.getLoginToken()).isEqualTo("UUID-12345678");
    }

    @Disabled("resendEmail 메서드를 수정하고나서 테스트 수정해야하므로 사용하지않음")
    @Test
    @DisplayName("이메일 재전송 성공")
    void reSendEmail_success() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678!!")
                .loginToken(UUID.randomUUID().toString())
                .build();

        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));

        // when
        sut.resendEmail(member.getEmail());

        // then
        then(mailService).should().send(any(EmailMessage.class));
    }

    @Test
    @DisplayName("이메일 재전송 실패 : 존재하지 않는 유저")
    void reSendEmail_fail() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678!!")
                .loginToken(UUID.randomUUID().toString())
                .build();

        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.empty());

        // when && then
        assertThrows(DoesNotExistMemberException.class, () -> sut.resendEmail(member.getEmail()));
        then(mailService).shouldHaveNoInteractions();
    }
}