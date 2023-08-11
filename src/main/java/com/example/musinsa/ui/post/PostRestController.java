package com.example.musinsa.ui.post;

import com.example.musinsa.common.exception.DuplicatedHashtagException;
import com.example.musinsa.common.security.CurrentMember;
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
    public void update(CurrentMember currentMember,
            @RequestBody @Valid PostUpdateRequest request,
            @PathVariable long postId
    ) {

        if (request.hashTags() == null || request.hashTags().isEmpty()) {
            postService.update(request.toDto(), currentMember.id(), postId);

        } else {
            Set<String> hashtags = validateDuplicateHashtag(request.hashTags());
            postService.update(request.toDtoWithHashtag(hashtags), currentMember.id(), postId);
        }

        postService.update(post,currentMember.id());
    }

}
