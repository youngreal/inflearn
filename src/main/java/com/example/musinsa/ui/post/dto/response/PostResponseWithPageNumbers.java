package com.example.musinsa.ui.post.dto.response;

import java.util.List;

public record PostResponseWithPageNumbers(
        List<PostResponse> posts,
        List<Integer> pageNumbers
) {

}
