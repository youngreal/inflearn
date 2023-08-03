package com.example.musinsa.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.musinsa.common.exception.DoesNotExistMemberException;
import com.example.musinsa.common.exception.DoesNotExistPostException;
import com.example.musinsa.domain.Hashtag;
import com.example.musinsa.domain.PostHashtag;
import com.example.musinsa.domain.member.domain.Member;
import com.example.musinsa.domain.post.domain.Post;
import com.example.musinsa.dto.PostDto;
import com.example.musinsa.infra.repository.member.MemberRepository;
import com.example.musinsa.infra.repository.post.HashtagRepository;
import com.example.musinsa.infra.repository.post.PostRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService sut;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private HashtagRepository hashtagRepository;


    @Test
    @DisplayName("포스트 작성 성공 : DB에 없는 새로운 해시태그를 입력받은경우")
    void write_success() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        PostDto postDto = PostDto.builder()
                .title("글제목1")
                .contents("글내용1")
                .hashTags(List.of("새로운자바","새로운스프링"))
                .build();

        Post postEntity = postDto.toEntity();
        Hashtag hashtag = Hashtag.createHashtag("새로운자바");
        Hashtag hashtag2 = Hashtag.createHashtag("새로운스프링");
        PostHashtag postHashtag = PostHashtag.createPostHashtag(postEntity, hashtag);
        PostHashtag postHashtag2 = PostHashtag.createPostHashtag(postEntity, hashtag2);

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(hashtagRepository.findByHashtagNameIn(postDto.hashTags())).willReturn(new ArrayList<>());

        // when
        sut.write(postDto,member.getId());

        // then
        ArgumentCaptor<Post> savedPost = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(savedPost.capture());
        Post post = savedPost.getValue();

        assertThat(post.getMember()).isEqualTo(member);
        assertThat(post.getPostHashtags()).isEqualTo(List.of(postHashtag, postHashtag2));
    }

    @Test
    @DisplayName("포스트 작성 성공2 : 기존에 있던 해시태그를 입력받은경우")
    void write_success2() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        PostDto postDto = PostDto.builder()
                .title("글제목1")
                .contents("글내용1")
                .hashTags(List.of("새로운자바","새로운스프링"))
                .build();

        Post postEntity = postDto.toEntity();
        Hashtag hashtag = Hashtag.createHashtag("기존에있던자바");
        Hashtag hashtag2 = Hashtag.createHashtag("새로운스프링");
        Hashtag hashtag3 = Hashtag.createHashtag("새로운자바");
        PostHashtag postHashtag = PostHashtag.createPostHashtag(postEntity, hashtag3);

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(hashtagRepository.findByHashtagNameIn(postDto.hashTags())).willReturn(List.of(hashtag,hashtag2));

        // when
        sut.write(postDto,member.getId());

        // then
        ArgumentCaptor<Post> savedPost = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(savedPost.capture());
        Post post = savedPost.getValue();

        assertThat(post.getMember()).isEqualTo(member);
        assertThat(post.getPostHashtags()).isEqualTo(List.of(postHashtag));
    }

    @Test
    @DisplayName("포스트 작성 성공3 : 글 제목, 본문만 있는경우")
    void write_success3() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        PostDto postDto = PostDto.builder()
                .title("글제목1")
                .contents("글내용1")
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));

        // when
        sut.write(postDto,member.getId());

        // then
        then(hashtagRepository).shouldHaveNoInteractions();
        ArgumentCaptor<Post> savedPost = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(savedPost.capture());
        Post post = savedPost.getValue();

        assertThat(post.getMember()).isEqualTo(member);
        assertThat(post.getPostHashtags()).isEmpty();
    }

    @Test
    @DisplayName("포스트 작성 실패 : 존재하지 않는 유저")
    void write_fail() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        PostDto postDto = PostDto.builder()
                .title("글제목1")
                .contents("글내용1")
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.write(postDto, member.getId()));
        then(hashtagRepository).shouldHaveNoInteractions();
        then(postRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("포스트 수정 성공")
    void update_success() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("글제목1")
                .contents("글내용1")
                .member(member)
                .build();

        Post updatePost = Post.builder()
                .id(1L)
                .title("수정제목1")
                .contents("수정내용1")
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(updatePost.getId())).willReturn(Optional.of(post));

        // when
        sut.update(updatePost,member.getId());

        // then
        assertThat(post.getTitle()).isEqualTo("수정제목1");
        assertThat(post.getContents()).isEqualTo("수정내용1");
    }

    @Test
    @DisplayName("포스트 수정 실패 : 존재하지 않는 유저")
    void update_fail() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post updatePost = Post.builder()
                .title("수정제목1")
                .contents("수정내용1")
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.update(updatePost, member.getId()));
    }

    @Test
    @DisplayName("포스트 수정 실패 : 존재하지 않는 게시글")
    void update_fail2() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("asdf1234@naver.com")
                .password("12345678")
                .build();

        Post updatePost = Post.builder()
                .title("수정제목1")
                .contents("수정내용1")
                .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(updatePost.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistPostException.class, () -> sut.update(updatePost, member.getId()));
    }
}