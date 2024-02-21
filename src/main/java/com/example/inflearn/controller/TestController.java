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

    /**
     * 메모리에 조회수 카운트하는 부분 배제한 테스트
     */
    @GetMapping("/viewCount5/{postId}")
    public PostDetailPageResponse test5(@PathVariable long postId) {
        return PostDetailPageResponse.from(postPerpormance.postDetail5(postId));
    }

    /**
     * 좋아요,댓글개수를 반영하는 부분 배제한 테스트
     * */
    @GetMapping("/viewCount6/{postId}")
    public PostDetailPageResponse test6(@PathVariable long postId) {
        return PostDetailPageResponse.from(postPerpormance.postDetail6(postId));
    }

    /**
     * 해시태그, 댓글 조인을 배제한 테스트
     * */
    @GetMapping("/viewCount7/{postId}")
    public PostDetailPageResponse test7(@PathVariable long postId) {
        return PostDetailPageResponse.from(postPerpormance.postDetail7(postId));
    }

    /**
     * I/O횟수를 줄인 테스트
     * */
    @GetMapping("/viewCount8/{postId}")
    public PostDetailPageResponse test8(@PathVariable long postId) {
        return PostDetailPageResponse.from(postPerpormance.postDetail8(postId));
    }

    /**
     * I/O횟수를 줄인 테스트
     * */
    @GetMapping("/viewCount9/{postId}")
    public PostDetailPageResponse test9(@PathVariable long postId) {
        return PostDetailPageResponse.from(postPerpormance.postDetail9(postId));
    }
}
