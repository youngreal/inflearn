package com.example.inflearn.ui.post.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PostSearch(
        @Min(1)
        int page,
        @Min(1)
        int size,
        @NotBlank
        String searchWord
) {

}
