package com.example.inflearn.dto;

import com.example.inflearn.domain.post.domain.Post;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public record PostDto(
        String title,
        String contents,
        int viewCount,
        Set<String> hashtags,
        LocalDateTime createdAt,
        LocalDateTime updateAt
) {

    public Post toEntity() {
        return Post.builder()
                .title(this.title)
                .contents(this.contents)
                .postHashtags(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PostDto from(Post post) {
        return PostDto.builder()
                .title(post.getTitle())
                .contents(post.getContents())
                .hashtags(post.getPostHashtags().stream()
                        .filter(postHashtag -> postHashtag.getHashtag() != null)
                        .map(postHashtag -> postHashtag.getHashtag().getHashtagName())
                        .collect(Collectors.toUnmodifiableSet()))
                .build();
    }

}
