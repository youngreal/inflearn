package com.example.musinsa.ui.member;

import com.example.musinsa.domain.Member;
import com.example.musinsa.domain.service.MemberService;
import com.example.musinsa.ui.member.dto.request.MemberJoinRequest;
import com.example.musinsa.ui.member.dto.response.MemberJoinResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
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
    public MemberJoinResponse join(@RequestBody @Valid MemberJoinRequest memberJoinRequest) {
        Member member = memberService.save(memberJoinRequest.toEntity(memberJoinRequest));
        return MemberJoinResponse.from(member);
    }

    //todo 프론트에서 RequestParam으로 받는게 편할까? DTO로 받는게 편할까?

    @GetMapping("/check-email-token")
    public void checkEmail(
            @RequestParam @NotBlank String emailToken,
            @RequestParam @NotBlank @Email String email
    ) {
        memberService.emailCheck(emailToken, email);
    }
}
