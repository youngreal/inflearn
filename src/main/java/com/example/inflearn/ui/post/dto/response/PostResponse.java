package com.example.inflearn.ui.post.dto.response;

import com.example.inflearn.domain.post.domain.PostStatus;
import com.example.inflearn.dto.PostDto;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;

@Builder
public record PostResponse(
        String nickname,
        String title,
        String contents,
        int viewCount,
        Set<String> hashtags,
        LocalDateTime createdAt,
        PostStatus postStatus
){

    public static PostResponse from(PostDto postDto) {
        return PostResponse.builder()
                .nickname(postDto.nickname())
                .title(postDto.title())
                .contents(postDto.contents())
                .viewCount(postDto.viewCount())
                .hashtags(postDto.hashtags())
                .createdAt(postDto.createdAt())
                .build();
    }
}

