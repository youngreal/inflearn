package com.example.inflearn.ui.member.dto.response;

import com.example.inflearn.domain.member.domain.Member;

public record MemberJoinResponse(
        String email
) {

    public static MemberJoinResponse from(Member member) {
        return new MemberJoinResponse(member.getEmail());
    }
}
