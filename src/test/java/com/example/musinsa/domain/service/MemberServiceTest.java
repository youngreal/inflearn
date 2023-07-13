package com.example.musinsa.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.example.musinsa.domain.Member;
import com.example.musinsa.infra.mail.EmailMessage;
import com.example.musinsa.infra.mail.MailService;
import com.example.musinsa.infra.repository.MemberRepository;
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
    @DisplayName("회원가입 성공 : 이미 존재하는 멤버")
    void test() {
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678!!")
                .build();

        // given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));

        // when
        assertThat(member.getEmailToken()).isNull();
        sut.save(member);

        // then
        assertThat(member.getEmailToken()).isNotNull();
        then(mailService).should().send(any(EmailMessage.class));
        then(memberRepository).should().save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패 : 이미 존재하는 멤버")
    void test2() {
        Member member = Member.builder()
                .email("asdf1234@naver.com")
                .password("12345678!!")
                .build();

        // given
        given(memberRepository.findById(anyLong())).willReturn(null);

        // when & then
        assertThrows(RuntimeException.class, () -> sut.save(member));
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
        given(memberRepository.findByEmail(member.getEmail())).willReturn(null);

        // when & then
        assertThrows(RuntimeException.class,
                () -> sut.emailCheck(member.getEmailToken(), member.getEmail()));
        //todo 로그인 메서드가 실행되지않는다.
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
        assertThrows(RuntimeException.class,
                () -> sut.emailCheck(member.getEmailToken(), member.getEmail()));
        //todo 로그인 메서드가 실행되지않는다.
    }


}