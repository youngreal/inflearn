package com.example.musinsa.domain.post.service;

import com.example.musinsa.common.exception.DoesNotExistPostException;
import com.example.musinsa.domain.post.domain.Post;
import com.example.musinsa.infra.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostQueryService {

    private final PostRepository postRepository;

    public Post postDetail(long postId) {
        return postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);
    }

    public Page<Post> searchPost(String searchWord, Pageable pageable) {
        if (searchWord == null || searchWord.isBlank()) {
            return Page.empty(pageable);
        }

        return postRepository.findByTitleOrContentsContaining(searchWord,searchWord, pageable);
    }

    public Page<Post> allList(Pageable pageable) {
        return postRepository.findAll(pageable);
    }
}
