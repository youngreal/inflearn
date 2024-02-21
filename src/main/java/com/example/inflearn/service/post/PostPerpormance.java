package com.example.inflearn.service.post;

import static java.util.stream.Collectors.toMap;

import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.domain.post.PostSearch;
import com.example.inflearn.domain.post.domain.PopularPost;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.mapper.post.PostMapper;
import com.example.inflearn.infra.redis.LikeCountRedisRepository;
import com.example.inflearn.infra.repository.dto.projection.PostHashtagDto;
import com.example.inflearn.infra.repository.post.PopularPostRepository;
import com.example.inflearn.infra.repository.post.PostRepository;
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
public class PostPerpormance {

    private final PostRepository postRepository;
    private final PopularPostRepository popularPostRepository;
    private final PostMemoryService postMemoryService;

    //todo 게시글 조회와 조회수가 +1 되는 로직은 트랜잭션 분리되어도 될것같은데..? 분리를 고려해보는게 맞을까?
    @Transactional
    public PostDto postDetail(long postId) {
        // 게시글 존재여부 검증
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        // 조회수 업데이트
        addViewCount(post);

        // 게시글 상세 내용 조회(해시태그, 댓글)
        PostDto postDetail = postRepository.postDetail(postId);
        postDetail.inputLikeCount(postId);
        postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
        postDetail.inputComments(postRepository.commentsBy(postDetail));
        return postDetail;
    }

    @Transactional
    public void postDetail2(long postId) {
        // 게시글 존재여부 검증
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        // 조회수 업데이트
        addViewCount(post);

        // 게시글 상세 내용 조회(해시태그, 댓글)
        postMemoryService.likeCount(postId);
        postMemoryService.commentCount(postId);
    }

    @Transactional
    public PostDto postDetail3(long postId) {
        // 게시글 존재여부 검증
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        // 게시글 상세 내용 조회(해시태그, 댓글)
        PostDto postDetail = postRepository.postDetail(postId);
        postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
        postDetail.inputComments(postRepository.commentsBy(postDetail));
        return postDetail;
    }


    @Transactional
    public PostDto postDetail4(long postId) {
        // 게시글 존재여부 검증
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        // 조회수 업데이트
        PopularPost popularPost = popularPostRepository.findByPostId(post.getId());
            // 인기글 테이블에 존재하지 않으면 update쿼리 발생, 존재하면 메모리에서 카운팅
        if (popularPost == null) {
            post.addViewCount();
            PostDto postDetail = postRepository.postDetail(postId);
            postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
            postDetail.inputComments(postRepository.commentsBy(postDetail));
            return postDetail;
        } else {
            postMemoryService.addViewCount(popularPost.getPostId());
            PostDto postDetail = postRepository.postDetail2(postId);
            postDetail.inputLikeCount(postMemoryService.likeCount(postId));
            postDetail.inputCommentCount(postMemoryService.commentCount(postId));
            postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
            postDetail.inputComments(postRepository.commentsBy(postDetail));
            return postDetail;
        }
    }

    @Transactional
    public PostDto postDetail5(long postId) {
        // 게시글 존재여부 검증
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        // 조회수 업데이트
        PopularPost popularPost = popularPostRepository.findByPostId(post.getId());
        // 인기글 테이블에 존재하지 않으면 update쿼리 발생, 존재하면 메모리에서 카운팅
        if (popularPost == null) {
            post.addViewCount();
            PostDto postDetail = postRepository.postDetail(postId);
            postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
            postDetail.inputComments(postRepository.commentsBy(postDetail));
            return postDetail;
        } else {
//            postMemoryService.addViewCount(popularPost.getPostId());
            PostDto postDetail = postRepository.postDetail2(postId);
            postDetail.inputLikeCount(postMemoryService.likeCount(postId));
            postDetail.inputCommentCount(postMemoryService.commentCount(postId));
            postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
            postDetail.inputComments(postRepository.commentsBy(postDetail));
            return postDetail;
        }
    }

    @Transactional
    public PostDto postDetail6(long postId) {
        // 게시글 존재여부 검증
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        // 조회수 업데이트
        PopularPost popularPost = popularPostRepository.findByPostId(post.getId());
        // 인기글 테이블에 존재하지 않으면 update쿼리 발생, 존재하면 메모리에서 카운팅
        if (popularPost == null) {
            post.addViewCount();
            PostDto postDetail = postRepository.postDetail(postId);
            postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
            postDetail.inputComments(postRepository.commentsBy(postDetail));
            return postDetail;
        } else {
            postMemoryService.addViewCount(popularPost.getPostId());
            PostDto postDetail = postRepository.postDetail2(postId);
//            postDetail.inputLikeCount(postMemoryService.likeCount(postId));
//            postDetail.inputCommentCount(postMemoryService.commentCount(postId));
            postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
            postDetail.inputComments(postRepository.commentsBy(postDetail));
            return postDetail;
        }
    }

    @Transactional
    public PostDto postDetail7(long postId) {
        // 게시글 존재여부 검증
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        // 조회수 업데이트
        PopularPost popularPost = popularPostRepository.findByPostId(post.getId());
        // 인기글 테이블에 존재하지 않으면 update쿼리 발생, 존재하면 메모리에서 카운팅
        if (popularPost == null) {
            post.addViewCount();
            PostDto postDetail = postRepository.postDetail(postId);
            postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
            postDetail.inputComments(postRepository.commentsBy(postDetail));
            return postDetail;
        } else {
            postMemoryService.addViewCount(popularPost.getPostId());
            PostDto postDetail = postRepository.postDetail2(postId);
            postDetail.inputLikeCount(postMemoryService.likeCount(postId));
            postDetail.inputCommentCount(postMemoryService.commentCount(postId));
//            postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
//            postDetail.inputComments(postRepository.commentsBy(postDetail));
            return postDetail;
        }
    }



    @Transactional
    public PostDto postDetail8(long postId) {
        // 게시글 존재여부 검증
        // 조회수 업데이트
        // 인기글 테이블에 존재하지 않으면 update쿼리 발생, 존재하면 메모리에서 카운팅
            postMemoryService.addViewCount(postId);
            PostDto postDetail = postRepository.postDetail2(postId);
            postDetail.inputLikeCount(postMemoryService.likeCount(postId));
            postDetail.inputCommentCount(postMemoryService.commentCount(postId));
            postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
            postDetail.inputComments(postRepository.commentsBy(postDetail));
            return postDetail;

    }

    @Transactional
    public PostDto postDetail9(long postId) {
        // 게시글 존재여부 검증
//        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        // 조회수 업데이트
//        addViewCount(post);

        // 게시글 상세 내용 조회(해시태그, 댓글)
        PostDto postDetail = postRepository.postDetail(postId);
//        postDetail.inputLikeCount(postId);
        postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
        postDetail.inputComments(postRepository.commentsBy(postDetail));
        return postDetail;
    }

    private void addViewCount(Post post) {
        PopularPost popularPost = popularPostRepository.findByPostId(post.getId());
        // 인기글 테이블에 존재하지 않으면 update쿼리 발생, 존재하면 메모리에서 카운팅
        if (popularPost == null) {
            post.addViewCount();
        } else {
            postMemoryService.addViewCount(popularPost.getPostId());
            postMemoryService.likeCount(popularPost.getPostId());
            postMemoryService.commentCount(popularPost.getPostId());
        }
    }
}
