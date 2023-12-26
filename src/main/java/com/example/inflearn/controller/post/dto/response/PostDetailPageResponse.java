package com.example.inflearn.controller.post.dto.response;

import com.example.inflearn.domain.post.domain.PostStatus;
import com.example.inflearn.dto.CommentDto;
import com.example.inflearn.domain.post.PostDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Builder;

@Builder
public record PostDetailPageResponse(
        String nickname,
        String title,
        String contents,
        long viewCount,
        long likeCount,
        Set<String> hashtags,
        List<CommentDto> commentDtoList,
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
                .commentDtoList(postDto.getComments())
                .createdAt(postDto.getCreatedAt())
                .updateAt(postDto.getUpdatedAt())
                .postStatus(postDto.getPostStatus())
                .build();
    }

}
