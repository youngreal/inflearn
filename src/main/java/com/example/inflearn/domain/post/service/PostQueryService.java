package com.example.inflearn.domain.post.service;

import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.dto.PostDto;
import com.example.inflearn.infra.repository.post.PostRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostQueryService {

    private final PaginationService paginationService;
    private final PostRepository postRepository;

    public PostDto postDetail(long postId) {
        return PostDto.from(postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new));
    }

    public List<PostDto> searchPost(String searchWord, int page, int size) {
        return postRepository.search(searchWord, paginationService.offSetWhenSearchPost(page), size).stream()
                .map(PostDto::from)
                .toList();
    }

    public List<PostDto> getPostsPerPage(int page, int size) {
        return postRepository.getPostsPerPage(page, size).stream()
                .map(PostDto::from)
                .toList();
    }

    public Long getPageCount(String searchWord, int page, int size) {
        return postRepository.countPage(searchWord, paginationService.offsetWhenGetPageNumbers(page), paginationService.sizeWhenGetPageNumbers(size));
    }
}
