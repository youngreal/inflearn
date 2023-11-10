package com.example.inflearn.infra.repository.dto.projection;

public record PopularPostDto(
        long postId,
        long likeCount
) {

}
