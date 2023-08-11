package com.example.musinsa.domain.post.service;

import com.example.musinsa.common.exception.DoesNotExistMemberException;
import com.example.musinsa.common.exception.DoesNotExistPostException;
import com.example.musinsa.common.exception.UnAuthorizationException;
import com.example.musinsa.domain.Hashtag;
import com.example.musinsa.domain.PostHashtag;
import com.example.musinsa.domain.member.domain.Member;
import com.example.musinsa.domain.post.domain.Post;
import com.example.musinsa.dto.PostDto;
import com.example.musinsa.infra.repository.member.MemberRepository;
import com.example.musinsa.infra.repository.post.HashtagRepository;
import com.example.musinsa.infra.repository.post.PostRepository;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final HashtagRepository hashtagRepository;

        Member member = memberRepository.findById(id)
    public void write(PostDto dto, long id) {
        Member member = memberRepository.findById(id).orElseThrow(DoesNotExistMemberException::new);
        Post post = dto.toEntity();

        if (dto.hashTags() != null && !dto.hashTags().isEmpty()) {
            List<String> inputStringHashtags = dto.hashTags(); // 입력받은 문자열 해시태그들
            List<Hashtag> matchingHashtags = hashtagRepository.findByHashtagNameIn(inputStringHashtags); // 입력받은 문자열중 DB에서 가져온 해시태그들

            List<Hashtag> hashtagResults;
            if (matchingHashtags.isEmpty()) {
                hashtagResults = inputStringHashtags.stream()
                        .map(Hashtag::createHashtag)
                        .toList();
            } else {
                hashtagResults = inputStringHashtags.stream()
                        .filter(inputStringHashtag -> matchingHashtags.stream()
                                .map(Hashtag::getHashtagName)
                                .noneMatch(matchingStringHashtag -> matchingStringHashtag.equals(inputStringHashtag)))
                        .map(Hashtag::createHashtag)
                        .toList();
            }
            addPostHashtagsFromPostAndHashtag(post, hashtagResults);
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

        post.update(dto.title(), dto.contents(), dto.hashTags());
    }
    }
}
