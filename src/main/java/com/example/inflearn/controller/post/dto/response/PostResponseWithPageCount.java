package com.example.inflearn.controller.post.dto.response;

import java.util.List;

public record PostResponseWithPageCount(
        List<PostResponse> posts,
        long pageCount
) {

}
