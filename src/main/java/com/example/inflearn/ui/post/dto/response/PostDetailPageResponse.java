package com.example.inflearn.ui.post.dto.response;

import com.example.inflearn.domain.post.domain.PostStatus;
import com.example.inflearn.dto.PostDto;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;

@Builder
public record PostDetailPageResponse(
        String nickname,
        LocalDateTime createdAt,
        LocalDateTime updateAt,
        int viewCount,
        String title,
        String contents,
        Set<String> hashtags,
        PostStatus postStatus
) {

    public static PostDetailPageResponse from(PostDto postDto) {
        return PostDetailPageResponse.builder()
                .nickname(postDto.nickname())
                .createdAt(postDto.createdAt())
                .updateAt(postDto.updateAt())
                .viewCount(postDto.viewCount())
                .title(postDto.title())
                .contents(postDto.contents())
                .hashtags(postDto.hashtags())
                .postStatus(postDto.postStatus())
                .build();
    }

}
