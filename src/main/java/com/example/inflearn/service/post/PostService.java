package com.example.inflearn.service.post;

import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.common.exception.UnAuthorizationException;
import com.example.inflearn.domain.post.domain.PostHashtag;
import com.example.inflearn.infra.repository.post.PopularPostRepository;
import com.example.inflearn.service.hashtag.HashtagService;
import com.example.inflearn.domain.member.Member;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.infra.repository.post.PostRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
todo 글 수정시 해시태그 수정/삭제가 발생해야하는데 이는 반드시 게시글수정시 바로 실행되지 않아도 되는 성질이라고 생각한다.
 또한 현실적으로 10개미만의 수정/삭제가 발생할 확률이 매우 높으므로 아직까지 크게 병목이 되진않는다.(현재 2.2ms 응답시간소요)
 만약 이부분이 병목이 된다면 트랜잭션을 분리하거나 비동기로 처리할수도 있을것같다. 현재는 병목이 아니다.
 *
 */

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class PostService {

    private final HashtagService hashtagService;
    private final PostMemoryService postMemoryService;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PopularPostRepository popularPostRepository;

    public void write(PostDto postDto, long id) {
        Member member = memberRepository.findById(id).orElseThrow(DoesNotExistMemberException::new);
        Post post = postDto.toEntity();
        updatePostAndHashtagEntity(postDto.getHashtags(), post, member);
        postRepository.save(post);
    }

    public void update(PostDto postDto, long memberId, long postId) {
        Member member = memberRepository.findById(memberId).orElseThrow(DoesNotExistMemberException::new);
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        if (!member.equals(post.getMember())) {
            throw new UnAuthorizationException("글 수정 권한이 없습니다");
        }

        updateHashtagEntity(postDto.getHashtags(), post);
        post.updateTitleAndContents(postDto.getTitle(), postDto.getContents());
    }

    public PostDto postDetail(long postId) {
        if (isPopularPost(postId)) {
            postMemoryService.addViewCount(postId);
            return popularPostDetail(postId);
        } else {
            updateViewCountInDb(postId);
            return regularPostDetail(postId);
        }
    }

    //todo 만약 관리해야할 인기글이 엄청많아진다면? => 성능을 테스트해보고 벌크업데이트성 로직이 추가될것같다.
    public void updateViewCountForPopularPosts() {
        for (Entry<Long, Long> memoryCacheEntry : postMemoryService.getViewCountStore().entrySet()) {
            log.info("entry = {}", memoryCacheEntry);
            Post post = postRepository.findById(memoryCacheEntry.getKey()).orElseThrow(DoesNotExistPostException::new);
            post.updateViewCountFromCache(memoryCacheEntry.getValue());
            postMemoryService.initViewCount(memoryCacheEntry.getKey());
        }
    }

    private void updateHashtagEntity(Set<String> hashtags, Post post) {
        List<PostHashtag> beforePostHashtags = new ArrayList<>(post.getPostHashtags());
        hashtagService.saveHashtagsWhenPostUpdate(post, hashtags);
        hashtagService.deleteHashtags(beforePostHashtags, hashtags);
    }

    private void updatePostAndHashtagEntity(Set<String> hashtags, Post post, Member member) {
        post.addMember(member);
        if (hashtags == null) {
            post.addPostHashtag(PostHashtag.createPostHashtag(post, null));
        } else {
            hashtagService.saveHashtags(post, hashtags);
        }
    }

    private boolean isPopularPost(long postId) {
        return popularPostRepository.findByPostId(postId) != null;
    }

    private void updateViewCountInDb(long postId) {
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);
        post.addViewCount();
    }

    private PostDto regularPostDetail(long postId) {
        PostDto postDetail = postRepository.postDetail(postId);
        postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
        postDetail.inputComments(postRepository.commentsBy(postDetail));
        return postDetail;
    }

    private PostDto popularPostDetail(long postId) {
        PostDto postDetail = postRepository.postDetailWithoutCountQuery(postId);
        postDetail.inputLikeCount(postMemoryService.likeCount(postId));
        postDetail.inputCommentCount(postMemoryService.commentCount(postId));
        postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
        postDetail.inputComments(postRepository.commentsBy(postDetail));
        return postDetail;
    }
}
