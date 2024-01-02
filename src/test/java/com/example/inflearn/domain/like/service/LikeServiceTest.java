package com.example.inflearn.domain.like.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.inflearn.common.exception.AlreadyLikeException;
import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.common.exception.DoesNotLikeException;
import com.example.inflearn.domain.like.domain.Like;
import com.example.inflearn.domain.member.domain.Member;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.repository.like.LikeRepository;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.infra.repository.post.PostRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    private final Member member = getMember();
    private final Post post = getEntity();

    @InjectMocks
    private LikeService sut;

    @Mock
    private PostRepository postRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private LikeRepository likeRepository;

    @Test
    void 게시글_좋아요_성공() {
        // given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
        given(likeRepository.findByMemberAndPost(member, post)).willReturn(null);

        // when
        sut.saveLike(member.getId(),post.getId());

        // then
        then(likeRepository).should().save(any(Like.class));
    }

    @Test
    void 게시글이_존재하지_않으면_좋아요_실패한다() {
        // given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(post.getId())).willReturn(Optional.empty());

        // when
        assertThrows(DoesNotExistPostException.class, () -> sut.saveLike(member.getId(), post.getId()));

        // then
        then(likeRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 존재하지_않는_유저는_좋아요_누를_수_없다() {
        // given
        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when
        assertThrows(DoesNotExistMemberException.class, () -> sut.saveLike(member.getId(), post.getId()));

        // then
        then(likeRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 이미_좋아요_누른_게시글은_좋아요_할_수_없다() {
        // given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
        given(likeRepository.findByMemberAndPost(member, post)).willReturn(Like.create(member,post));

        // when
        assertThrows(AlreadyLikeException.class, () -> sut.saveLike(member.getId(), post.getId()));

        // then
        then(likeRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 게시글_좋아요_취소_성공() {
        // given
        Like like = Like.create(member, post);
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
        given(likeRepository.findByMemberAndPost(member, post)).willReturn(like);

        // when
        sut.unLike(member.getId(),post.getId());

        // then
        then(likeRepository).should().delete(like);
    }

    @Test
    void 게시글_좋아요_취소_실패() {
        // given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
        given(likeRepository.findByMemberAndPost(member, post)).willReturn(null);

        // when
        assertThrows(DoesNotLikeException.class, () -> sut.unLike(member.getId(), post.getId()));

        // then
        then(likeRepository).shouldHaveNoMoreInteractions();
    }

    private Member getMember() {
        return Member.builder()
                .id(1L)
                .email("email@naver.com")
                .password("12345678")
                .build();
    }

    private Post getEntity() {
        return Post.builder()
                .id(1L)
                .title("제목1234")
                .contents("본문1234")
                .build();
    }
}