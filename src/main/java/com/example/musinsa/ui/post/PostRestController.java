package com.example.musinsa.ui.post;

import com.example.musinsa.common.exception.DuplicatedHashtagException;
import com.example.musinsa.common.security.CurrentMember;
import com.example.musinsa.domain.post.domain.Post;
import com.example.musinsa.domain.post.service.PostService;
import com.example.musinsa.ui.post.dto.request.PostUpdateRequest;
import com.example.musinsa.ui.post.dto.request.PostWriteRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PostRestController {

    private final PostService postService;

    @PostMapping("/posts")
    public void write(
            CurrentMember currentMember,
            @RequestBody @Valid PostWriteRequest postWriteRequest
    ) {
        Post post = postWriteRequest.toEntity(postWriteRequest);

        if (post.getTags().size() != postWriteRequest.hashTags().stream().distinct().count()) {
            throw new DuplicatedHashtagException();
        }

        postService.write(post,currentMember.id());
    }

    @PutMapping("/posts/{postId}")
    public void update(CurrentMember currentMember,
            @RequestBody @Valid PostUpdateRequest postUpdateRequest,
            @PathVariable long postId
    ) {
        Post post = postUpdateRequest.toEntity(postUpdateRequest, postId);

        if (post.getTags().size() != postUpdateRequest.hashTags().stream().distinct().count()) {
            throw new DuplicatedHashtagException();
        }

        postService.update(post,currentMember.id());
    }

}
