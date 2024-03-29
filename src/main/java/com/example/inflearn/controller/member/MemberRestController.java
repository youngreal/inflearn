package com.example.inflearn.controller.member;

import com.example.inflearn.common.security.LoginedMember;
import com.example.inflearn.domain.member.Member;
import com.example.inflearn.service.member.MemberService;
import com.example.inflearn.controller.member.dto.request.MemberJoinRequest;
import com.example.inflearn.controller.member.dto.request.MemberLoginRequest;
import com.example.inflearn.controller.member.dto.response.MemberJoinResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
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
@RestController
public class MemberRestController {

    private final MemberService memberService;

    @PostMapping("/members")
    public MemberJoinResponse sendSignUpEmail(@RequestBody @Valid MemberJoinRequest memberJoinRequest) {
        Member member = memberService.signUp(memberJoinRequest.toEntity());
        return MemberJoinResponse.from(member);
    }

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
        memberService.resendEmail(email);
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid MemberLoginRequest memberLoginRequest) {
        String sessionToken = memberService.login(memberLoginRequest.toEntity());
        return responseWithCookie(sessionToken);
    }

    @PostMapping("/logout")
    public void logout(LoginedMember loginedMember) {
        memberService.logout(loginedMember.id());
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
