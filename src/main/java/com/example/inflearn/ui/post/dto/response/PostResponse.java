package com.example.inflearn.ui.post.dto.response;

import com.example.inflearn.domain.post.domain.PostStatus;
import com.example.inflearn.dto.PostDto;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;

@Builder
public record PostResponse(
        Long postId,
        String nickname,
        String title,
        String contents,
        int viewCount,
        Long likeCount,
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
                .hashtags(postDto.getHashtags())
                .createdAt(postDto.getCreatedAt())
                .build();
    }
}

