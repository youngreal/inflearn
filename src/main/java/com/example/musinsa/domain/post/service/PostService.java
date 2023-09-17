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
    private final MemberRepository memberRepository;

    public void write(PostDto dto, long id) {
        Member member = memberRepository.findById(id).orElseThrow(DoesNotExistMemberException::new);
        Post post = dto.toEntity();

        if (dto.hashtags().isEmpty()) {
            post.addPostHashtag(PostHashtag.createPostHashtag(post, null));
        } else {
            hashtagService.saveNewHashtagsWhenPostWrite(post, dto.hashtags());
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
        hashtagService.saveHashtagsWhenPostUpdate(post, dto.hashtags());
        hashtagService.deleteHashtags(beforePostHashtags, dto.hashtags());
        post.updateTitleAndContents(dto.title(), dto.contents());
    }
}
