package com.example.inflearn.ui.post.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentContents(
        @NotBlank
        String contents
) {
}
