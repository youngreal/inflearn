package com.example.inflearn.controller;

import com.example.inflearn.controller.post.dto.response.PostDetailPageResponse;
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

    /**
     * 게시글 상세조회 제외하고 테스트
     * @
     */
    @GetMapping("/viewCount2/{postId}")
    public void test2(@PathVariable long postId) {
        postPerpormance.postDetail2(postId);
    }

    /**
     * 조회수 업데이트 제외하고 테스트
     */
    @GetMapping("/viewCount3/{postId}")
    public void test3(@PathVariable long postId) {
        postPerpormance.postDetail3(postId);
    }

    /**
     * count 쿼리가 발생하지않게 메모리에 인기글의 좋아요수, 댓글수를 저장해두고 반영
     */
    @GetMapping("/viewCount4/{postId}")
    public PostDetailPageResponse test4(@PathVariable long postId) {
        return PostDetailPageResponse.from(postPerpormance.postDetail4(postId));
    }
}
