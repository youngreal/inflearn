package com.example.musinsa.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.musinsa.domain.Hashtag;
import com.example.musinsa.domain.PostHashtag;
import com.example.musinsa.domain.post.domain.Post;
import com.example.musinsa.infra.repository.post.HashtagRepository;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HashtagServiceTest {

    @InjectMocks
    private HashtagService sut;

    @Mock
    private HashtagRepository hashtagRepository;

    private Post post;
    private Hashtag hashtag;
    private Hashtag hashtag2;
    private Hashtag hashtag3;


    @BeforeEach
    void setup() {
        post = Post.builder()
                .title("제목1")
                .contents("내용1")
                .postHashtags(new ArrayList<>())
                .build();
    }

    @DisplayName("post 작성시 새롭게 저장할 해시태그가 존재하지않는다.")
    @MethodSource
    @ParameterizedTest
    void saveWhenPostWrite(Set<String> input, Set<Hashtag> existingDB, Set<Hashtag> expected) {
        // given
        given(hashtagRepository.findByHashtagNameIn(input)).willReturn(existingDB);

        // when
        int beforePostHashtagSize = post.getPostHashtags().size();
        sut.saveNewHashtagsWhenPostWrite(input, post);
        int afterPostHashtagSize = post.getPostHashtags().size();

        // then
        assertThat(beforePostHashtagSize).isEqualTo(afterPostHashtagSize);
        then(hashtagRepository).should().saveAll(expected);
    }

    static Stream<Arguments> saveWhenPostWrite() {
        return Stream.of(
                arguments(
                        Set.of("java", "spring", "aws"),
                        Set.of(Hashtag.createHashtag("java"), Hashtag.createHashtag("spring"),
                                Hashtag.createHashtag("aws")),
                        Set.of()
                ),
                arguments(
                        Set.of(),
                        Set.of(),
                        Set.of()
                )
        );
    }

    @DisplayName("post 작성시 새롭게 저장할 해시태그들을 저장한다.")
    @MethodSource
    @ParameterizedTest
    void saveWhenPostWrite2(Set<String> input, Set<Hashtag> existingDB, Set<Hashtag> expected) {
        // given
        given(hashtagRepository.findByHashtagNameIn(input)).willReturn(existingDB);

        // when
        int beforePostHashtagSize = post.getPostHashtags().size();
        sut.saveNewHashtagsWhenPostWrite(input, post);
        int afterPostHashtagSize = post.getPostHashtags().size();

        // then
        assertThat(beforePostHashtagSize).isNotEqualTo(afterPostHashtagSize);
        then(hashtagRepository).should().saveAll(expected);
    }

    static Stream<Arguments> saveWhenPostWrite2() {
        return Stream.of(
                arguments(
                        Set.of("java", "spring", "aws"),
                        Set.of(Hashtag.createHashtag("java"), Hashtag.createHashtag("spring")),
                        Set.of(Hashtag.createHashtag("aws"))
                ),
                arguments(
                        Set.of("java","spring55"),
                        Set.of(Hashtag.createHashtag("java"),Hashtag.createHashtag("spring")),
                        Set.of(Hashtag.createHashtag("spring55"))
                )
        );
    }

    @DisplayName("글 수정시 새로운 해시태그가 추가된다")
    @MethodSource
    @ParameterizedTest
    void saveNewHashtagsWhenPostUpdate(Set<String> input,Set<Hashtag> DBHashtag, Set<Hashtag> saveExpected) {
        // given
        given(hashtagRepository.findByHashtagNameIn(input)).willReturn(DBHashtag);

        // when
        int beforePostHashtagSize = post.getPostHashtags().size();
        sut.saveNewHashtagsWhenPostUpdate(post, input);
        int afterPostHashtagSize = post.getPostHashtags().size();

        // then
        assertThat(beforePostHashtagSize).isNotEqualTo(afterPostHashtagSize);
        then(hashtagRepository).should().saveAll(saveExpected);
    }


    static Stream<Arguments> saveNewHashtagsWhenPostUpdate() {
        return Stream.of(
                arguments(
                        Set.of("java", "spring", "aws"),
                        Set.of(Hashtag.createHashtag("java"), Hashtag.createHashtag("spring")),
                        Set.of(Hashtag.createHashtag("aws"))
                ),
                arguments(
                        Set.of("java","spring55"),
                        Set.of(Hashtag.createHashtag("java"),Hashtag.createHashtag("spring")),
                        Set.of(Hashtag.createHashtag("spring55"))
                )
        );
    }

    @DisplayName("글 수정시 기존 해시태그가 삭제된다")
    @MethodSource
    @ParameterizedTest
    void deleteNewHashtagsWhenPostUpdate(Set<String> input,Set<Hashtag> DBHashtag, Set<Hashtag> saveExpected) {
        // given
        Hashtag hashtag1 = Hashtag.createHashtag("java");
        PostHashtag postHashtag = PostHashtag.createPostHashtag(post, hashtag1);
        post.addPostHashtag(postHashtag);
        hashtag1.addPostHashtag(postHashtag);

        // when
        sut.deleteHashtags(post.getPostHashtags(), input);

        // then
        then(hashtagRepository).should().deleteAll(saveExpected);
    }

    static Stream<Arguments> deleteNewHashtagsWhenPostUpdate() {
        return Stream.of(
                arguments(
                        Set.of(),
                        Set.of(),
                        Set.of(Hashtag.createHashtag("java"))
                )
        );
    }
}