package com.example.inflearn.domain.post.service;

import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.common.exception.UnAuthorizationException;
import com.example.inflearn.domain.post.domain.PostHashtag;
import com.example.inflearn.domain.hashtag.service.HashtagService;
import com.example.inflearn.domain.member.domain.Member;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.infra.repository.post.PostRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
todo 글 수정시 해시태그 수정/삭제가 발생해야하는데 이는 반드시 게시글수정시 바로 실행되지 않아도 되는 성질이라고 생각한다.
 또한 현실적으로 10개미만의 수정/삭제가 발생할 확률이 매우 높으므로 아직까지 크게 병목이 되진않는다.(현재 2.2ms 응답시간소요)
 만약 이부분이 병목이 된다면 트랜잭션을 분리하거나 비동기로 처리할수도 있을것같다. 현재는 병목이 아니다.
 *
 */

@Transactional
@RequiredArgsConstructor
@Service
public class PostService {

    private final HashtagService hashtagService;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public void write(PostDto dto, long id) {
        Member member = memberRepository.findById(id).orElseThrow(DoesNotExistMemberException::new);
        Post post = dto.toEntityForWrite();

        if (dto.getHashtags().isEmpty()) {
            post.addPostHashtag(PostHashtag.createPostHashtag(post, null));
        } else {
            hashtagService.saveHashtags(post, dto.getHashtags());
        }

        post.addMember(member);
        postRepository.save(post);
    }

    public void update(PostDto dto, long memberId, long postId) {
        Member member = memberRepository.findById(memberId).orElseThrow(DoesNotExistMemberException::new);
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        if (!member.equals(post.getMember())) {
            throw new UnAuthorizationException("글 수정 권한이 없습니다");
        }

        List<PostHashtag> beforePostHashtags = new ArrayList<>(post.getPostHashtags());
        hashtagService.saveHashtagsWhenPostUpdate(post, dto.getHashtags());
        hashtagService.deleteHashtags(beforePostHashtags, dto.getHashtags());
        post.updateTitleAndContents(dto.getTitle(), dto.getContents());
    }

    //todo 만약 관리해야할 인기글이 엄청많아진다면? => 성능을 테스트해보고 벌크업데이트성 로직이 추가될것같다.
    public void updateViewCountForPopularPosts(Map<Object, Object> popularPostEntries) {
        for (Entry<Object, Object> entry : popularPostEntries.entrySet()) {
            Post post = postRepository.findById((Long) entry.getKey()).orElseThrow();
            post.updateViewCountFromCache((Long) entry.getValue());
        }
            //todo 왜 업데이트 쿼리를 날리지않고 DB에서 가져와야하는가? 제대로 알고 넘어가기
    }
}
