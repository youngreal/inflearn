package com.example.inflearn.controller.member.dto.response;

import com.example.inflearn.domain.member.Member;

public record MemberJoinResponse(
        String email
) {

    public static MemberJoinResponse from(Member member) {
        return new MemberJoinResponse(member.getEmail());
    }
}
