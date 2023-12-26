package com.example.inflearn.domain.post.service;

import static java.util.stream.Collectors.toMap;

import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.redis.LikeCountRedisRepository;
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

    private final PaginationService paginationService;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final LikeCountRedisRepository likeCountRedisRepository;

    //todo 게시글 조회와 조회수가 +1 되는 로직은 트랜잭션 분리되어도 될것같은데..? 분리를 고려해보는게 맞을까?
    //todo 테스트 하기도 힘들다
    //todo 좋아요, 댓글을 추가해보고 성능 개선해봐야한다.
    // v1 레디스 hyperloglog사용
    @Transactional
    public PostDto postDetail(long postId) {
        // 게시글 존재여부 검증
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        // 조회수 업데이트
        addViewCount(post);

        // 게시글 상세 내용 조회(해시태그, 댓글)
        PostDto postDetail = postRepository.postDetail(postId);
        postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
        postDetail.inputComments(postRepository.commentsBy(postDetail));
        return postDetail;
    }

    public List<PostDto> searchPost(PostSearch postSearch) {
        List<PostDto> postDtos = postMapper.search(postSearch.searchWord(), paginationService.calculateOffSet(postSearch.page()), postSearch.size(), postSearch.sort());
        setHashtagsWithJoin(postDtos);
        return postDtos;
    }

    /**
     * 성능테스트 비교를 위한 Like검색쿼리
     */
    public List<PostDto> searchPostV2(PostSearch postSearch) {
        List<PostDto> postDtos = postMapper.searchV2(postSearch.searchWord(), paginationService.calculateOffSet(postSearch.page()), postSearch.size(), postSearch.sort());
        setHashtagsWithJoin(postDtos);
        return postDtos;
    }

    public List<PostDto> searchPostWithHashtag(PostSearch postSearch) {
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

    // page 1 , size = 20
    // offset 0, 200
    // page 11 , size = 20 size = x 10
    // offset 200, 20
    public long getPageCount(int pageNumber, int postQuantityPerPage) {
        List<Long> pageCount = postRepository.getPageCount(paginationService.offsetForTotalPageNumbers(pageNumber), paginationService.sizeForTotalPageNumbers(postQuantityPerPage));
        return pageCount.size();
    }

    // 현재 시간으로부터 -7일 사이에 있는 게시글중 좋아요 개수가 가장 많은 게시글을 5개까지만 가져온다
    public void updatePopularPosts() {
        // 레디스에 있는 게시글과 popularPosts의 likeCount들을 비교해서 5개만 레디스에 업데이트한다
        Map<Long, Long> popularPosts = getPopularPosts().stream()
                .collect(toMap(PostDto::getPostId, PostDto::getLikeCount));

        likeCountRedisRepository.updatePopularPosts(popularPosts);
    }

    private List<PostDto> getPopularPosts() {
        LocalDate endDay = LocalDate.now();
        LocalDate firstDay = endDay.minusDays(7);
        return PostDto.toDto(postRepository.findPopularPostByDate(firstDay, endDay));
    }

    private void setHashtagsWithJoin(List<PostDto> postDtos) {
        List<PostHashtagDto> postHashtagDtos = postRepository.postHashtagsByPostDtos(postDtos);
        Map<Long, List<PostHashtagDto>> postHashtagMap = postHashtagDtos.stream()
                .collect(Collectors.groupingBy(PostHashtagDto::postId));

        postDtos.forEach(postDto -> postDto.inputHashtags(postHashtagMap.get(postDto.getPostId())));
    }

    // todo 부하테스트 해봐야한다. 레디스는 싱글스레드이기떄문에 조회 요청이 레디스에 엄청몰리면 어느정도 트래픽까집 버틸수있는지.
    // todo 만약 hyperloglog로 한다면 ? 성능이 어떻게 나올까?
    private void addViewCount(Post post) {
        // validation: 레디스에서 인기글을 가져오고, 레디스에 없다면 DB에서 가져오자
        // 인기글이아니라면(레디스에없다면) 조회수 +1 업데이트, 레디스에있으면 레디스에 조회수 카운팅
        if (likeCountRedisRepository.getViewCount(post.getId()) == null) {
            post.plusViewCount();
        } else {
            likeCountRedisRepository.updateViewCountToCache(post.getId());
        }
    }
}
