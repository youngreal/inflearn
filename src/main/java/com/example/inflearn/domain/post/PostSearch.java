package com.example.inflearn.domain.post;

import com.example.inflearn.common.annotation.validation.sort.SortEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PostSearch(
        @Min(1)
        int page,
        @Min(1)
        int size,
        @NotBlank
        String searchWord,
        @SortEnum(enumClass = PostSort.class, ignoreCase = true)
        String sort
) {

        public static PostSearch of(int page, int size, String searchWord) {
                return new PostSearch(page, size, searchWord, null);
        }

        public static PostSearch of(int page, int size, String searchWord, String sort) {
                return new PostSearch(page, size, searchWord, sort);
        }
}
