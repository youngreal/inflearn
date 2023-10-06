package com.example.inflearn.ui.post.dto.response;

import com.example.inflearn.domain.post.domain.PostStatus;
import com.example.inflearn.dto.PostDto;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;

@Builder
public record PostDetailPageResponse(
        String nickname,
        String title,
        String contents,
        int viewCount,
        Long likeCount,
        Set<String> hashtags,
        LocalDateTime createdAt,
        LocalDateTime updateAt,
        PostStatus postStatus
) {

    public static PostDetailPageResponse from(PostDto postDto) {
        return PostDetailPageResponse.builder()
                .nickname(postDto.getNickname())
                .title(postDto.getTitle())
                .contents(postDto.getContents())
                .viewCount(postDto.getViewCount())
                .likeCount(postDto.getLikeCount())
                .hashtags(postDto.getHashtags())
                .createdAt(postDto.getCreatedAt())
                .updateAt(postDto.getUpdatedAt())
                .postStatus(postDto.getPostStatus())
                .build();
    }

}
