package com.example.inflearn.service.post;

import static java.util.stream.Collectors.toMap;

import com.example.inflearn.common.exception.SearchWordLengthException;
import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.infra.repository.dto.projection.PostHashtagDto;
import com.example.inflearn.infra.mapper.post.PostMapper;
import com.example.inflearn.infra.repository.post.PostRepository;
import com.example.inflearn.domain.post.PostSearch;
import java.time.LocalDate;
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

    private static final int SEARCH_WORD_MIN_LENGTH = 2;
    private final PaginationService paginationService;
    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public List<PostDto> searchPost(PostSearch postSearch) {
        if (postSearch.searchWord().length() < SEARCH_WORD_MIN_LENGTH) {
            throw new SearchWordLengthException();
        }

        List<PostDto> postDtos = postMapper.search(postSearch.searchWord(), paginationService.calculateOffSet(postSearch.page()), postSearch.size(), postSearch.sort());
        setHashtagsWithJoin(postDtos);
        return postDtos;
    }

    public List<PostDto> searchPostWithHashtag(PostSearch postSearch) {
        if (postSearch.searchWord().length() < SEARCH_WORD_MIN_LENGTH) {
            throw new SearchWordLengthException();
        }

        List<Long> postIds = postRepository.findPostIdsByHashtagSearchWord(postSearch.searchWord());
        List<PostDto> postDtos = postRepository.searchWithHashtag(postSearch.searchWord(), paginationService.calculateOffSet(postSearch.page()), postSearch.size(), postSearch.sort(), postIds);
        setHashtagsWithJoin(postDtos);
        return postDtos;
    }

    public List<PostDto> getPostsPerPage(int pageNumber, int postQuantityPerPage, String sort) {
        List<PostDto> postDtos = postRepository.getPostsPerPage(paginationService.calculateOffSet(pageNumber), postQuantityPerPage, sort);
        setHashtagsWithJoin(postDtos);
        return postDtos;
    }

    public Long getPageCountWithSearchWord(PostSearch postSearch) {
        return postRepository.countPageWithSearchWord(postSearch.searchWord(), paginationService.offsetForTotalPageNumbers(postSearch.page()), paginationService.sizeForTotalPageNumbers(postSearch.size()));
    }

    public Long getPageCountWithHashtagSearchWord(PostSearch postSearch) {
        return postRepository.countPageWithHashtagSearchWord(postSearch.searchWord(), paginationService.offsetForTotalPageNumbers(postSearch.page()), paginationService.sizeForTotalPageNumbers(postSearch.size()));
    }

    /*
      page 1 , size = 20
      offset 0, 200
      page 11 , size = 20 size = x 10
      offset 200, 20
     */
    public long getPageCount(int pageNumber, int postQuantityPerPage) {
        List<Long> pageCount = postRepository.getPageCount(paginationService.offsetForTotalPageNumbers(pageNumber), paginationService.sizeForTotalPageNumbers(postQuantityPerPage));
        return pageCount.size();
    }

    public Map<Long, Long> updatePopularPostsForSchedulerTest() {
        return popularPosts().stream()
                .collect(toMap(PostDto::getPostId, PostDto::getLikeCount));
    }

    private List<PostDto> popularPosts() {
        LocalDate endDay = LocalDate.now();
        LocalDate firstDay = endDay.minusDays(300);
        return PostDto.toDto(postRepository.findPopularPostByDate(firstDay, endDay));
    }

    private void setHashtagsWithJoin(List<PostDto> postDtos) {
        List<PostHashtagDto> postHashtagDtos = postRepository.postHashtagsByPostDtos(postDtos);
        Map<Long, List<PostHashtagDto>> postHashtagMap = postHashtagDtos.stream()
                .collect(Collectors.groupingBy(PostHashtagDto::postId));

        postDtos.forEach(postDto -> postDto.inputHashtags(postHashtagMap.get(postDto.getPostId())));
    }
}
