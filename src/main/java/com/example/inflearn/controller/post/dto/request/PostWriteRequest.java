package com.example.inflearn.controller.post.dto.request;

import com.example.inflearn.domain.post.PostDto;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;

@Builder
public record PostWriteRequest(
        @NotBlank
        String title,
        @NotBlank
        String contents,
        List<String> hashtags
        @AllowNullButNoBlank
) {

    public PostDto toDto() {
        return PostDto.builder()
                .title(this.title)
                .contents(this.contents)
                .hashtags(new HashSet<>())
                .build();
    }

    public PostDto toDtoWithHashtag(Set<String> hashtags) {
        return PostDto.builder()
                .title(this.title)
                .contents(this.contents)
                .hashtags(hashtags)
                .build();
    }
}
