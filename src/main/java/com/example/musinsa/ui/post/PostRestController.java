package com.example.musinsa.ui.post;

import com.example.musinsa.common.exception.DuplicatedHashtagException;
import com.example.musinsa.common.security.CurrentMember;
import com.example.musinsa.domain.post.service.PaginationService;
import com.example.musinsa.domain.post.service.PostQueryService;
import com.example.musinsa.domain.post.service.PostService;
import com.example.musinsa.ui.post.dto.request.PostPaging;
import com.example.musinsa.ui.post.dto.response.PostResponse;
import com.example.musinsa.ui.post.dto.request.PostUpdateRequest;
import com.example.musinsa.ui.post.dto.request.PostWriteRequest;
import com.example.musinsa.ui.post.dto.response.PostResponseWithPageNumbers;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
public class PostRestController {

    private final PostService postService;
    private final PostQueryService postQueryService;
    private final PaginationService paginationService;

    @PostMapping("/posts")
    public void write(
            CurrentMember currentMember,
            @RequestBody @Valid PostWriteRequest request
    ) {

        if (request.hashtags() == null || request.hashtags().isEmpty()) {
            postService.write(request.toDto(), currentMember.id());

        } else {
            Set<String> hashtags = validateDuplicateHashtag(request.hashtags());
            postService.write(request.toDtoWithHashtag(hashtags), currentMember.id());
        }
 }

    @PutMapping("/posts/{postId}")
    public void update(
            CurrentMember currentMember,
            @RequestBody @Valid PostUpdateRequest request,
            @PathVariable long postId
    ) {

        if (request.hashtags() == null || request.hashtags().isEmpty()) {
            postService.update(request.toDto(), currentMember.id(), postId);

        } else {
            Set<String> hashtags = validateDuplicateHashtag(request.hashtags());
            postService.update(request.toDtoWithHashtag(hashtags), currentMember.id(), postId);
        }
    }

    /**
     * select * from post order by id desc limit 0,20;
     * select count(*)가 70초 이상걸리는 성능..
     * page는 파라미터로 받는다.
     */

    /*
    방법 1. 서브쿼리를 이용한 방법

    503페이지를 준다
    503 - 1의자리 + 1 = 500
    503 + 10 - 503의 1의자리 = 510

    select * from post order by id desc limit page*10,size(20개씩);
    select count(*)
    from (
            select *
            from post
            order by id desc
            limit page*10,size
        ) As subquery;

    위의쿼리 결과가 0 인경우 => 찾으시는 페이지가 없습니다.
    위의 쿼리의 결과 < size 인경우 => 마지막 페이지임. 즉 얘가 endPage가 되어야함
    위의 쿼리의 결과 == size 인경우 => 해당 페이지의 마지막 페이지(page+10 - page의1의자리)로 query를 한번더 실행하고
        마지막페이지마저 size랑 결과가같다면 endpage가 (page+10-page의1의자리)가 맞음
        마지막페이지 < size

    select count(*) from post where

    방법 2. 게시글의 전체 개수를 관리하는 테이블을 하나 만들고 관리하는방법
    totalCount가 필요할때마다 해당 테이블에서 select해서 개수를 가져온다.
    => 게시글의 수정/삭제 발생할때마다 업데이트 발생

    방법 3.
    select count(*)
    from (
    select * from post
    order by id desc
     limit 10000
     ) As subquery;

    count쿼리가 최대 10000개를 넘지않고 최대 개수가 12000개라고 가정하고 12000개 이상의 게시글을 조회할때는 검색엔진에 맡기는방법?

    단점
    - 검색엔진에 DB데이터가 최신화가 안될수가있다.
    - DB와 검색엔진간 대기시간이 생길수있다.
    12000 개 기준으로 0.016~0.032초 나와서 더 가져올수도 있을것같다. 어느정도 수치까지가 괜찮을지는 조금더 생각해봐야한다.

     */
    @GetMapping("/posts")
    public PostResponseWithPageNumbers getPosts(@ModelAttribute @Valid PostPaging postPaging) {
        //20개의 데이터
        List<PostResponse> posts = postQueryService.getPostsPerPage(postPaging.size(), postPaging.page()).stream().map(PostResponse::from).toList();
        List<Integer> pageNumbers = paginationService.getPageNumbers(postPaging.page(), postQueryService.getTotalCount());

        return new PostResponseWithPageNumbers(posts, pageNumbers);
    }

    // 현재 쿼리 member,post,tag 조인해서 한번에 쿼리에 가져온다.
    @GetMapping("/posts/{postId}")
    public PostResponse postDetail(@PathVariable long postId) {
        return PostResponse.from(postQueryService.postDetail(postId));
    }

    //todo 현재 검색시 쿼리가 3번나간다(post 조건조회, member 조회, tag 조회)
    @GetMapping("/posts/search")
    public PostResponseWithPageNumbers searchedPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
            @RequestParam String searchWord
    ) {
        Page<PostResponse> posts = postQueryService.searchPost(searchWord, pageable)
                .map(PostResponse::from);
        List<Integer> pageNumbers = paginationService.getPageNumbers(pageable.getPageNumber(),
                posts.getTotalPages());

        return new PostResponseWithPageNumbers(posts, pageNumbers);
    }

    private Set<String> validateDuplicateHashtag(List<String> requestHashtag) {
        Set<String> hashtags = new HashSet<>();
        for (String hashtag : requestHashtag) {
            if (hashtags.contains(hashtag)) {
                throw new DuplicatedHashtagException();
            }
            hashtags.add(hashtag);
        }

        return hashtags;
    }
    //todo 삭제API
}
