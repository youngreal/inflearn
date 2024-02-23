package com.example.inflearn.controller.post.dto.request;

import com.example.inflearn.common.annotation.validation.hashtag.AllowNullButNoBlank;
import com.example.inflearn.domain.post.PostDto;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import lombok.Builder;

@Builder
public record PostWriteRequest(
        @NotBlank
        String title,
        @NotBlank
        String contents,
        @AllowNullButNoBlank
        Set<String> hashtags
) {

    public PostDto toDto(Set<String> hashtags) {
        return PostDto.builder()
                .title(this.title)
                .contents(this.contents)
                .hashtags(hashtags)
                .build();
    }
}
