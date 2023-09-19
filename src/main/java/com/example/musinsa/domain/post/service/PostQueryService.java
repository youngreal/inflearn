package com.example.musinsa.domain.post.service;

import com.example.musinsa.common.exception.DoesNotExistPostException;
import com.example.musinsa.domain.post.domain.Post;
import com.example.musinsa.infra.repository.post.PostRepository;
import java.util.List;
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

    public List<Post> searchPost(String searchWord, Pageable pageable) {
        if (searchWord == null || searchWord.isBlank()) {
            return List.of();
        }

        return postRepository.findByTitleOrContentsContaining(searchWord,searchWord, pageable);
    }

    //todo 해당 테스트는 SpringBootTest? dataJpaTest? 아니면 작성안한다?
    public List<Post> allList(Pageable pageable) {
        return postRepository.getPostPerPage(pageable.getPageSize(), pageable.getPageNumber());
    }

    public int getTotalCount() {
        List<Post> posts = postRepository.totalCount();
        return posts.size();
    }
}
