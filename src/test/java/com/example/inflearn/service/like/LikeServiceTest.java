package com.example.inflearn.service.like;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.inflearn.common.exception.AlreadyLikeException;
import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.DoesNotExistPostException;
import com.example.inflearn.common.exception.DoesNotLikeException;
import com.example.inflearn.domain.like.Like;
import com.example.inflearn.domain.member.Member;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.repository.like.LikeRepository;
import com.example.inflearn.infra.repository.member.MemberRepository;
import com.example.inflearn.infra.repository.post.PostRepository;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
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

    @InjectMocks
    private LikeService sut;

    @Mock
    private PostRepository postRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private LikeRepository likeRepository;

    private final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build();

    private final Member member = fixtureMonkey.giveMeBuilder(Member.class)
            .setNotNull("id").sample();
    private final Post post = fixtureMonkey.giveMeBuilder(Post.class)
            .setNotNull("id").sample();

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

        // when & then
        assertThrows(DoesNotExistPostException.class, () -> sut.saveLike(member.getId(), post.getId()));
    }

    @Test
    void 존재하지_않는_유저는_좋아요_누를_수_없다() {
        // given
        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.saveLike(member.getId(), post.getId()));
    }

    @Test
    void 이미_좋아요_누른_게시글은_좋아요_할_수_없다() {
        // given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
        given(likeRepository.findByMemberAndPost(member, post)).willReturn(Like.create(member, post));

        // when & then
        assertThrows(AlreadyLikeException.class, () -> sut.saveLike(member.getId(), post.getId()));
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

        // when & then
        assertThrows(DoesNotLikeException.class, () -> sut.unLike(member.getId(), post.getId()));
    }
}