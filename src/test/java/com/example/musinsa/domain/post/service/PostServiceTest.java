package com.example.musinsa.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
import com.example.musinsa.ui.post.dto.request.PostUpdateRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        Member member = createMember(1L, "asdf1234@naver.com","password12345678");
        PostDto postDto = writeDto("글제목1", "글내용1", Set.of("새로운자바", "새로운스프링"));

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
        Member member = createMember(1L, "asdf1234@naver.com","12345678");
        PostDto postDto = writeDto("글제목1", "글내용1", Set.of("새로운자바", "새로운스프링"));

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
        Member member = createMember(1L, "asdf1234@naver.com","12345678");
        PostDto postDto = writeDto("글제목1", "글내용1", null);

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
        Member member = createMember(1L, "asdf1234@naver.com","12345678");
        PostDto postDto = writeDto("글제목1", "글내용1", null);

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
        Member member = createMember(1L, "asdf1234@naver.com","12345678");
        Post post = createPost(member);

        PostUpdateRequest dto = PostUpdateRequest.builder()
                .title("수정제목1")
                .contents("수정내용1")
                .build();

        long requestPostId = 1L;

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(requestPostId)).willReturn(Optional.of(post));

        // when
        sut.update(dto.toDto(),member.getId(), requestPostId);

        // then
        assertThat(post.getTitle()).isEqualTo("수정제목1");
        assertThat(post.getContents()).isEqualTo("수정내용1");
    }

    @Test
    @DisplayName("포스트 수정 실패 : 존재하지 않는 유저")
    void update_fail() {
        // given
        Member member = createMember(1L, "asdf1234@naver.com","12345678");

        PostUpdateRequest dto = PostUpdateRequest.builder()
                .title("수정제목1")
                .contents("수정내용1")
                .build();
        long requestPostId = 1L;
        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistMemberException.class, () -> sut.update(dto.toDto(), member.getId(), requestPostId));
    }

    @Test
    @DisplayName("포스트 수정 실패 : 존재하지 않는 게시글")
    void update_fail2() {
        // given
        Member member = createMember(1L, "asdf1234@naver.com","12345678");

        PostUpdateRequest dto = PostUpdateRequest.builder()
                .title("수정제목1")
                .contents("수정내용1")
                .build();

        long requestPostId = 1L;

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(requestPostId)).willReturn(Optional.empty());

        // when & then
        assertThrows(DoesNotExistPostException.class, () -> sut.update(dto.toDto(), member.getId(), requestPostId));
    }

    @Test
    @DisplayName("포스트 수정 실패 : 수정권한이 없는 유저")
    void update_fail3() {
        // given
        Member member = createMember(1L, "asdf1234@naver.com","12345678");
        Member member2 = createMember(2L, "qwer1234@naver.com","12345678");
        Post post = createPost(member2);

        PostUpdateRequest dto = PostUpdateRequest.builder()
                .title("수정제목1")
                .contents("수정내용1")
                .build();

        long requestPostId = 1L;

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(postRepository.findById(requestPostId)).willReturn(Optional.of(post));

        // when & then
        assertThrows(UnAuthorizationException.class, () -> sut.update(dto.toDto(), member.getId(), requestPostId));
    }

    private Post createPost(Member member) {
        return Post.builder()
                .id(1L)
                .title("글제목1")
                .contents("글내용1")
                .member(member)
                .build();
    }

    private Member createMember(long id, String email,String password) {
        return Member.builder()
                .id(id)
                .email(email)
                .password(password)
                .build();
    }

    private PostDto writeDto(String title, String contents, Set<String> hashtags) {
        return PostDto.builder()
                .title(title)
                .contents(contents)
                .hashTags(hashtags)
                .build();
    }
}