package com.example.inflearn.infra.repository.dto.projection;

public record PostHashtagDto (
        Long postId,
        String hashtagName
){

    public static PostHashtagDto create(String hashtagName) {
        return new PostHashtagDto(null, hashtagName);
    }
}
