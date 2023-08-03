package com.example.musinsa.ui.post.dto.request;

import com.example.musinsa.dto.PostDto;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;

@Builder
public record PostWriteRequest(
        @NotBlank
        String title,
        List<String> hashTags,
        @NotBlank
        String contents
) {

    public PostDto toDto() {
        if (this.hashTags == null || this.hashTags.isEmpty()) {
            return PostDto.builder()
                    .title(this.title)
                    .contents(this.contents)
                    .hashTags(new ArrayList<>())
                    .build();
        }

        return PostDto.builder()
                .title(this.title)
                .hashTags(this.hashTags)
                .contents(this.contents)
                .build();
    }
}
