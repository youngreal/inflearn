package com.example.inflearn.ui.post;

import com.example.inflearn.common.exception.DuplicatedHashtagException;
import com.example.inflearn.common.exception.SearchWordLengthException;
import com.example.inflearn.common.security.LoginedMember;
import com.example.inflearn.domain.comment.service.CommentService;
import com.example.inflearn.domain.like.service.LikeService;
import com.example.inflearn.domain.post.service.PostQueryService;
import com.example.inflearn.domain.post.service.PostService;
import com.example.inflearn.ui.post.dto.request.PostCommentContents;
import com.example.inflearn.ui.post.dto.request.PostReplyContents;
import com.example.inflearn.domain.post.PostSearch;
import com.example.inflearn.ui.post.dto.request.PostPaging;
import com.example.inflearn.ui.post.dto.response.PostDetailPageResponse;
import com.example.inflearn.ui.post.dto.response.PostResponse;
import com.example.inflearn.ui.post.dto.request.PostUpdateRequest;
import com.example.inflearn.ui.post.dto.request.PostWriteRequest;
import com.example.inflearn.ui.post.dto.response.PostResponseWithPageCount;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
public class PostRestController {

    private static final int SEARCH_WORD_MIN_LENGTH = 2;
    private final PostService postService;
    private final PostQueryService postQueryService;
    private final LikeService likeService;
    private final CommentService commentService;

    @PostMapping("/posts")
    public void write(
            LoginedMember loginedMember,
            @RequestBody @Valid PostWriteRequest request
    ) {

        if (request.hashtags() == null || request.hashtags().isEmpty()) {
            postService.write(request.toDto(), loginedMember.id());

        } else {
            Set<String> hashtags = validateDuplicatedHashtag(request.hashtags());
            postService.write(request.toDtoWithHashtag(hashtags), loginedMember.id());
        }
 }

    //todo 글 수정시 해시태그 유무 DB에서 꺼낼때는 DB에없었는데, 동시에 해시태그가 삽입된다면? 동시성 문제 발생할수있다.
    //todo 글 수정시 해시태그를 삭제해야해서 삭제하는순간에 조회하는 요청이 온다면?
    @PutMapping("/posts/{postId}")
    public void update(
            LoginedMember loginedMember,
            @RequestBody @Valid PostUpdateRequest request,
            @PathVariable long postId
    ) {

        if (request.hashtags() == null || request.hashtags().isEmpty()) {
            postService.update(request.toDto(), loginedMember.id(), postId);

        } else {
            Set<String> hashtags = validateDuplicatedHashtag(request.hashtags());
            postService.update(request.toDtoWithHashtag(hashtags), loginedMember.id(), postId);
        }
    }

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

   /*
    todo
    - 최신순으로 조회한다(ㅇ)
    - 해시태그로 검색한다
    - 댓글많은순으로 조회한다(ㅇ)
    - 좋아요순으로 조회한다(ㅇ)
     */

    /*
     ## Eager일때
     select post offset limit
     select member in
     select post,hashtag from post_hashtag left join hashtag
     where post id in
     select post offset limit

     ## Lazy일때
     select post offset limit
     select * from post_hashtag where post_id in
     select hashtag * from hashtag where hashtag_id in
     select post offset limit

     ## Lazy + fetch join 일때
     select post from post join member order by id desc offset limit
     select * from post_hashtag where post_id in
     select * from hashtag where hashtag_id in
     select post offset limit
     */
    //todo 조인이 13초 걸리는데 이유가 뭘까? 인덱스를 태워야한다..
    //todo PostPaging으로 Service에 넘겨야할까? primitive type으로 변경해서 넘겨야할까.. postPaging은 ui에서 요청받는 dto인데..?
    @GetMapping("/posts")
    public PostResponseWithPageCount getPosts(@ModelAttribute @Valid PostPaging postPaging) {
        List<PostResponse> posts = postQueryService.getPostsPerPage(postPaging.page(), postPaging.size(), postPaging.sort()).stream()
                .map(PostResponse::from)
                .toList();

        long pageCount = postQueryService.getPageCount(postPaging.page(), postPaging.size());
        return new PostResponseWithPageCount(posts, pageCount);
    }

    @GetMapping("/posts/{postId}")
    public PostDetailPageResponse postDetail(@PathVariable long postId) {
        return PostDetailPageResponse.from(postQueryService.postDetail(postId));
    }

    - 댓글많은순으로 조회한다(지금못함)
    - 좋아요순으로 조회한다(지금못함)
     */

    @GetMapping("/posts/search")
    public PostResponseWithPageCount searchedPosts(@ModelAttribute @Valid PostSearch postSearch) {
        if (postSearch.searchWord().length() < SEARCH_WORD_MIN_LENGTH) {
            throw new SearchWordLengthException();
        }
        /*
        파라미터 타입을 풀어서쓸까, 객체로 넘길까 고민하다가 파라미터로 넘기게되면 PostSearch가 변경되는경우 아래의 레이어에도 변경의 영향이 미칠수있으며
         객체로 넘기게되면 PostSearch에 추가요구사항(필드추가)이 생기더라도 이 아래의 레이어에선 코드변경이 없을수도 있는경우가 있기때문에 우선 객체로 넘기는 방식을 선택해본다.
         */
        List<PostResponse> posts = postQueryService.searchPost(postSearch).stream()
                .map(PostResponse::from)
                .toList();

        long pageCount = postQueryService.getPageCountWithSearchWord(postSearch);
        return new PostResponseWithPageCount(posts, pageCount);
    }


    @PostMapping("/posts/{postId}/likes")
    public void like(LoginedMember loginedMember,@PathVariable long postId) {
        likeService.saveLike(loginedMember.id(), postId);
    }

    @DeleteMapping("/posts/{postId}/likes")
    public void unLike(LoginedMember loginedMember,@PathVariable long postId) {
        likeService.unLike(loginedMember.id(), postId);
    }

    @PostMapping("/posts/{postId}/comments")
    public void comment(LoginedMember loginedMember, @RequestBody @Valid PostCommentContents postCommentContents,
            @PathVariable long postId) {
        commentService.saveComment(loginedMember.id(), postId, postCommentContents.contents());
    }

    @PostMapping("/comments/{parentCommentId}/reply")
    public void reply(LoginedMember loginedMember, @RequestBody @Valid PostReplyContents postReplyContents, @PathVariable long parentCommentId) {
        commentService.saveReply(loginedMember.id(), parentCommentId, postReplyContents.contents());
    }

    private Set<String> validateDuplicatedHashtag(List<String> requestHashtag) {
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
