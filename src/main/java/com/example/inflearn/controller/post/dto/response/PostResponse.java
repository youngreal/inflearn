package com.example.inflearn.controller.post.dto.response;

import com.example.inflearn.domain.post.domain.PostStatus;
import com.example.inflearn.domain.post.PostDto;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;

@Builder
public record PostResponse(
        Long postId,
        String nickname,
        String title,
        String contents,
        long viewCount,
        long likeCount,
        long commentCount,
        Set<String> hashtags,
        LocalDateTime createdAt,
        PostStatus postStatus
){

    public static PostResponse from(PostDto postDto) {
        return PostResponse.builder()
                .postId(postDto.getPostId())
                .nickname(postDto.getNickname())
                .title(postDto.getTitle())
                .contents(postDto.getContents())
                .viewCount(postDto.getViewCount())
                .likeCount(postDto.getLikeCount())
                .commentCount(postDto.getCommentCount())
                .hashtags(postDto.getHashtags())
                .createdAt(postDto.getCreatedAt())
                .build();
    }
}

