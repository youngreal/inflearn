package com.example.musinsa.domain.service.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.musinsa.common.exception.AlreadyExistMemberException;
import com.example.musinsa.common.exception.DoesNotExistEmailException;
import com.example.musinsa.common.exception.DoesNotExistMemberException;
import com.example.musinsa.common.exception.WrongEmailTokenException;
import com.example.musinsa.domain.member.domain.Member;
import com.example.musinsa.domain.member.service.MemberService;
import com.example.musinsa.infra.mail.EmailMessage;
import com.example.musinsa.infra.mail.MailService;
import com.example.musinsa.infra.repository.member.MemberRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService sut;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MailService mailService;

    @Test
    @DisplayName("회원가입 성공")
    void test() {
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678!!")
                .build();

        // given
        given(memberRepository.existsByEmail(member.getEmail())).willReturn(false);

        // when
        assertThat(member.getEmailToken()).isNull();
        sut.signUp(member);

        // then
        assertThat(member.getEmailToken()).isNotNull();
        then(mailService).should().send(any(EmailMessage.class));
        then(memberRepository).should().save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패 : 이미 존재하는 멤버")
    void test2() {
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
    @DisplayName("이메일 체크 성공하면 Member의 isVerify필드의 상태를 변경한다")
    void email_check_success() {
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
    @DisplayName("이메일 체크 실패 : 존재하지 않는 이메일 ")
    void test3() {
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
    @DisplayName("이메일 체크 실패2 : 유효하지않은 이메일,토큰번호 ")
    void test4() {
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
    @DisplayName("로그인 성공후 토큰을 발급한다.")
    void test5() {
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
    @DisplayName("로그인 실패 : 존재하지 않는 유저")
    void test6() {
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
    @DisplayName("로그아웃 성공 후 토큰에 null을 입력한다.")
    void test7() {
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
    @DisplayName("로그아웃 실패 : 존재하지 않는 유저")
    void test8() {
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