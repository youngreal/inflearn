package com.example.inflearn.domain.post.service;

import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.dto.PostDto;
import com.example.inflearn.dto.PostHashtagDto;
import com.example.inflearn.infra.mapper.post.PostMapper;
import com.example.inflearn.infra.repository.post.PostRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostQueryService {

    private final PaginationService paginationService;
    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public PostDto postDetail(long postId) {
        postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);
        PostDto postDetail = postRepository.postDetail(postId);
        postDetail.setHashtags(postRepository.postHashtagsBy(postDetail));
        return postDetail;
    }

    public List<PostDto> searchPost(String searchWord, int page, int size) {
        List<PostDto> postDtos = postMapper.search(searchWord, paginationService.calculateOffSet(page), size);
        setHashtagsWithJoin(postDtos);
        return postDtos;
    }

    public List<PostDto> getPostsPerPage(int page, int size) {
        List<PostDto> postDtos = postRepository.getPostsPerPage(paginationService.calculateOffSet(page), size);
        setHashtagsWithJoin(postDtos);
        return postDtos;
    }

    public Long getPageCountWithSearchWord(String searchWord, int page, int size) {
        return postRepository.countPageWithSearchWord(searchWord, paginationService.calculateOffsetWhenGetPageNumbers(page), paginationService.sizeWhenGetPageNumbers(size));
    }

    // page 1 , size = 20
    // offset 0, 200
    // page 11 , size = 20 size = x 10
    // offset 200, 20
    public long getPageCount(int page, int size) {
        List<Long> pageCount = postRepository.getPageCount(paginationService.calculateOffsetWhenGetPageNumbers(page), paginationService.sizeWhenGetPageNumbers(size));
        return pageCount.size();
    }

    private void setHashtagsWithJoin(List<PostDto> postDtos) {
        List<PostHashtagDto> postHashtagDtos = postRepository.postHashtagsByPostDtos(postDtos);

        if (postHashtagDtos == null) {
            postHashtagDtos = List.of();
        }

        Map<Long, List<PostHashtagDto>> postHashtagMap = postHashtagDtos.stream()
                .collect(Collectors.groupingBy(PostHashtagDto::postId));

        postDtos.forEach(postDto -> postDto.setHashtags(postHashtagMap.get(postDto.getPostId())));
    }
}
