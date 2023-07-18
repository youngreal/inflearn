package com.example.musinsa.ui.member.dto.response;

import com.example.musinsa.domain.member.domain.Member;

public record MemberJoinResponse(
        String email
) {

    public static MemberJoinResponse from(Member member) {
        return new MemberJoinResponse(member.getEmail());
    }
}
