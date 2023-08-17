package com.example.musinsa.ui.member;

import com.example.musinsa.common.security.CurrentMember;
import com.example.musinsa.domain.member.domain.Member;
import com.example.musinsa.domain.member.service.MemberService;
import com.example.musinsa.ui.member.dto.request.MemberJoinRequest;
import com.example.musinsa.ui.member.dto.request.MemberLoginRequest;
import com.example.musinsa.ui.member.dto.response.MemberJoinResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Validated
@Slf4j
@RestController
public class MemberRestController {

    private final MemberService memberService;

    @PostMapping("/members")
    public MemberJoinResponse sendSignUpEmail(@RequestBody @Valid MemberJoinRequest memberJoinRequest) {
        Member member = memberService.signUp(memberJoinRequest.toEntity(memberJoinRequest));
        return MemberJoinResponse.from(member);
    }

    //todo 프론트에서 RequestParam으로 받는게 편할까? DTO로 받는게 편할까?

    @GetMapping("/check-email-token")
    public ResponseEntity<Void> checkEmail(
            @RequestParam @NotBlank String emailToken,
            @RequestParam @NotBlank @Email String email
    ) {
        Member member = memberService.checkEmail(emailToken, email);
        String sessionToken = memberService.login(member);
        return responseWithCookie(sessionToken);
    }

    @GetMapping("/resend-email")
    public void resendEmail(@RequestParam String email) {
        //todo 이메일 재전송 제한시간? 20분에1번 설정?
        memberService.resendEmail(email);
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid MemberLoginRequest memberLoginRequest) {
        String sessionToken = memberService.login(memberLoginRequest.toEntity(memberLoginRequest));
        return responseWithCookie(sessionToken);
    }

    @PostMapping("/logout")
    public void logout(CurrentMember currentMember) {
        memberService.logout(currentMember.id());
    }

    private ResponseEntity<Void> responseWithCookie(String sessionToken) {
        ResponseCookie cookie = ResponseCookie.from("SESSION", sessionToken)
                .domain("localhost") //todo 개발할땐 localhost지만 개발,운영에서는 변경될수있다. (profile 설정값으로 설정파일에서 추가하면 좋을것이다)
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ofDays(30))
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }
}
