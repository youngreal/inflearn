package com.example.inflearn.dto;

import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.domain.post.domain.PostStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
querydsl dto projections 에서 기본생성자가 필요했었고 record로는 원활하게 진행되지않아서 DTO로 변환하였다.
 */
@ToString
@Getter
@NoArgsConstructor
public class PostDto {

    private Long postId;
    private String nickname;
    private String title;
    private String contents;
    private int viewCount;
    private Long likeCount;
    private Set<String> hashtags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PostStatus postStatus;

    @Builder
    public PostDto(Long postId, String nickname, String title, String contents, int viewCount,
            Long likeCount, Set<String> hashtags, LocalDateTime createdAt, LocalDateTime updatedAt,
            PostStatus postStatus) {
        this.postId = postId;
        this.nickname = nickname;
        this.title = title;
        this.contents = contents;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.hashtags = hashtags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.postStatus = postStatus;
    }

    public Post toEntity() {
        return Post.builder()
                .title(this.title)
                .contents(this.contents)
                .postHashtags(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void setHashtags(List<PostHashtagDto> postHashtagDtos) {
        if (postHashtagDtos != null) {
            this.hashtags = postHashtagDtos.stream()
                    .map(PostHashtagDto::hashtagName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());
        }
    }
}
