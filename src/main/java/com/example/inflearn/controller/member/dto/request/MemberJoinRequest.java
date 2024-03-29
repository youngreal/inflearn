package com.example.inflearn.controller.member.dto.request;

import com.example.inflearn.domain.member.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MemberJoinRequest(
        @NotBlank @Email
        String email,
        @NotBlank @Size(min = 8, max = 20)
        @Pattern(regexp = "^[a-zA-Z0-9\\p{Punct}]+$")
        String password
) {

        public Member toEntity() {
                return Member.builder()
                        .email(this.email)
                        .password(this.password)
                        .build();
        }

}
