package com.example.inflearn.infra.repository.dto.projection;

public record PostCommentDto(
        Long commentId,
        Long parentCommentId,
        String contents
) {

}
