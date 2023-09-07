package com.example.musinsa.domain.post.service;

import com.example.musinsa.common.exception.DoesNotExistMemberException;
import com.example.musinsa.common.exception.DoesNotExistPostException;
import com.example.musinsa.common.exception.UnAuthorizationException;
import com.example.musinsa.domain.PostHashtag;
import com.example.musinsa.domain.member.domain.Member;
import com.example.musinsa.domain.post.domain.Post;
import com.example.musinsa.dto.PostDto;
import com.example.musinsa.infra.repository.member.MemberRepository;
import com.example.musinsa.infra.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class PostService {

    private final HashtagService hashtagService;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public void write(PostDto dto, long id) {
        Member member = memberRepository.findById(id).orElseThrow(DoesNotExistMemberException::new);
        Post post = dto.toEntity();

        if (!dto.hashtags().isEmpty()) {
            hashtagService.saveNewHashtagsWhenPostWrite(dto.hashtags(),post);
        }

        post.addMember(member);
        post.addPostHashtag(PostHashtag.createPostHashtag(post, null));
        postRepository.save(post);
    }

    public void update(PostDto dto, long memberId, long postId) {
        Member member = memberRepository.findById(memberId).orElseThrow(DoesNotExistMemberException::new);
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        if (!member.equals(post.getMember())) {
            throw new UnAuthorizationException("글 수정 권한이 없습니다");
        }

        hashtagService.saveNewHashtagsWhenPostUpdate(post, dto.hashtags());
        hashtagService.deleteHashtags(post.getPostHashtags(), dto.hashtags());
        post.updateTitleAndContents(dto.title(), dto.contents());
    }
}
