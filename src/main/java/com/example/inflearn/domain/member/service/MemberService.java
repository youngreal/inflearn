package com.example.inflearn.domain.member.service;

import com.example.inflearn.common.exception.AlreadyExistMemberException;
import com.example.inflearn.common.exception.DoesNotExistEmailException;
import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.UnAuthorizationException;
import com.example.inflearn.common.exception.WrongEmailTokenException;
import com.example.inflearn.domain.member.domain.Member;
import com.example.inflearn.domain.member.event.Events;
import com.example.inflearn.domain.member.event.MailSentEvent;
import com.example.inflearn.infra.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public Member signUp(Member member) {
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new AlreadyExistMemberException();
        }

        member.generateEmailToken();
        Events.raise(new MailSentEvent(member));
        return memberRepository.save(member);
    }

    public Member checkEmail(String emailToken, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new DoesNotExistEmailException("잘못된 이메일 주소입니다."));

        if (!member.isValidEmailToken(emailToken)) {
            throw new WrongEmailTokenException("잘못된 이메일 토큰입니다.");
        }

        member.completeEmailVerify();
        return member;
    }

    public String login(Member member) {
        Member newMember = memberRepository.findByEmailAndPassword(member.getEmail(), member.getPassword()).orElseThrow(DoesNotExistMemberException::new);
        if (newMember.isLogined()) {
            throw new UnAuthorizationException("이미 로그인된 유저입니다");
        }

        if (!newMember.isVerifiedEmail()) {
            throw new UnAuthorizationException("이메일 인증이 완료되지 않은 유저입니다");
        }
        newMember.generateLoginToken();
        return newMember.getLoginToken();
    }

    public void logout(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(DoesNotExistMemberException::new);
        member.invalidateToken();
    }

    @Transactional(readOnly = true)
    public void resendEmail(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(DoesNotExistMemberException::new);
        Events.raise(new MailSentEvent(member));
    }
}
