package com.example.inflearn.controller;

import com.example.inflearn.service.post.PostPerpormance;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final PostPerpormance postPerpormance;

    @GetMapping("/viewCount/{postId}")
    public void test(@PathVariable long postId) {
        postPerpormance.postDetail(postId);
    }
}
