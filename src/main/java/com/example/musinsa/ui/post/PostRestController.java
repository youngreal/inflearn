package com.example.musinsa.ui.post;

import com.example.musinsa.common.exception.DuplicatedHashtagException;
import com.example.musinsa.common.security.CurrentMember;
import com.example.musinsa.domain.post.service.PaginationService;
import com.example.musinsa.domain.post.service.PostQueryService;
import com.example.musinsa.domain.post.service.PostService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
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

        if (request.hashTags() == null || request.hashTags().isEmpty()) {
            postService.write(request.toDto(), currentMember.id());

        } else {
            Set<String> hashtags = validateDuplicateHashtag(request.hashTags());
            postService.write(request.toDtoWithHashtag(hashtags), currentMember.id());
        }
 }

    @PutMapping("/posts/{postId}")
    public void update(
            CurrentMember currentMember,
            @RequestBody @Valid PostUpdateRequest request,
            @PathVariable long postId
    ) {

        if (request.hashTags() == null || request.hashTags().isEmpty()) {
            postService.update(request.toDto(), currentMember.id(), postId);

        } else {
            Set<String> hashtags = validateDuplicateHashtag(request.hashTags());
            postService.update(request.toDtoWithHashtag(hashtags), currentMember.id(), postId);
        }
    }

    //todo 현재 쿼리 총 4번(member , post, tag, count쿼리)
    @GetMapping("/posts")
    public PostResponseWithPageNumbers allPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse> posts = postQueryService.allList(pageable).map(PostResponse::from);
        List<Integer> pageNumbers = paginationService.getPageNumbers(pageable.getPageNumber(), posts.getTotalPages());

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
