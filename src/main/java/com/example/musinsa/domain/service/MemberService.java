package com.example.musinsa.domain.service;

import com.example.musinsa.common.exception.AlreadyExistMemberException;
import com.example.musinsa.common.exception.DoesNotExistEmailException;
import com.example.musinsa.common.exception.DoesNotExistMemberException;
import com.example.musinsa.common.exception.WrongEmailTokenException;
import com.example.musinsa.domain.Member;
import com.example.musinsa.infra.mail.EmailMessage;
import com.example.musinsa.infra.mail.MailService;
import com.example.musinsa.infra.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MailService mailService;

    public Member signUp(Member member) {
        if (memberRepository.existsById(member.getId())) {
            throw new AlreadyExistMemberException("이미 존재하는 회원입니다");
        }

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newMember.getEmail())
                .subject("[인프런] 회원가입을 위해 메일인증을 해주세요.")
                .message("안녕하세요, 인프랩입니다. 아래 메일 인증 버튼을 눌러 회원가입을 완료해주세요.\n"
                        + "/check-email-token?emailToken=" + newMember.getEmailToken() +
                                "&email=" + newMember.getEmail())
                .build();

        mailService.send(emailMessage);
        return memberRepository.save(newMember);
    }

    //todo CQRS?
    @Transactional(readOnly = true)
    public Member emailCheck(String emailToken, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new DoesNotExistEmailException("잘못된 이메일 주소입니다."));

        if (!member.isValidEmailToken(emailToken)) {
            throw new WrongEmailTokenException("잘못된 이메일 토큰입니다.");
        }

        return member;
    }

    public String login(Member member) {
        Member newMember = memberRepository.findByEmailAndPassword(member.getEmail(), member.getPassword()).orElseThrow(() -> new DoesNotExistMemberException("존재하지않는 회원입니다."));
        newMember.generateLoginToken();
        return newMember.getLoginToken();
    }

    public void logout(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new DoesNotExistMemberException("존재하지 않는 유저입니다"));
        member.invalidateToken();
    }
}
