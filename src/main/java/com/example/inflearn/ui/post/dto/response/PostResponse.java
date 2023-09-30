package com.example.inflearn.ui.post.dto.response;

import com.example.inflearn.domain.post.domain.PostStatus;
import com.example.inflearn.dto.PostDto;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;

@Builder
public record PostResponse(
        String title,
        String contents,
        int viewCount,
        Set<String> hashtags,
        LocalDateTime createdAt,
        LocalDateTime updateAt,
        PostStatus postStatus
){

    public static PostResponse from(PostDto postDto) {
        return PostResponse.builder()
                .title(postDto.title())
                .contents(postDto.contents())
                .viewCount(postDto.viewCount())
                .hashtags(postDto.hashtags())
                .createdAt(postDto.createdAt())
                .updateAt(postDto.updateAt())
                .build();
    }
}

