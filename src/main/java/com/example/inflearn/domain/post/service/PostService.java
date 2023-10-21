package com.example.inflearn.domain.post.service;

import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.common.exception.UnAuthorizationException;
import com.example.inflearn.domain.PostHashtag;
import com.example.inflearn.domain.hashtag.service.HashtagService;
import com.example.inflearn.domain.member.domain.Member;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.domain.post.PostDto;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.infra.repository.post.PostRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
@Slf4j
public class PostService {

    private final HashtagService hashtagService;
    private final PostRepository postRepository;
    //todo usecase 레이어 생성해서 분리해보자.
    private final MemberRepository memberRepository;

    public void write(PostDto dto, long id) {
        Member member = memberRepository.findById(id).orElseThrow(DoesNotExistMemberException::new);
        Post post = dto.toEntityForWrite();

        if (dto.getHashtags().isEmpty()) {
            post.addPostHashtag(PostHashtag.createPostHashtag(post, null));
        } else {
            hashtagService.saveNewHashtagsWhenPostWrite(post, dto.getHashtags());
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
    public void updateViewCountForPopularPosts(List<Long> postIds) {
        for (Long postId : postIds) {
            Post post = postRepository.findById(postId).orElseThrow();
            post.plusViewCount();
            //todo 왜 업데이트 쿼리를 날리지않고 DB에서 가져와야하는가? 제대로 알고 넘어가기
        }
    }
}
