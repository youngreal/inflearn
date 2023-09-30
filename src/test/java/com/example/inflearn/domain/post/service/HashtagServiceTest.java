package com.example.inflearn.domain.post.service;

import static com.example.inflearn.domain.Hashtag.createHashtag;
import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.inflearn.domain.Hashtag;
import com.example.inflearn.domain.PostHashtag;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.repository.post.HashtagRepository;
import java.util.ArrayList;
import java.util.List;
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

    @BeforeEach
    void setup() {
        post = Post.builder()
                .title("제목1")
                .contents("내용1")
                .postHashtags(new ArrayList<>())
                .build();
    }

    @DisplayName("post 작성시 새롭게 저장할 해시태그가 존재하지않는다.")
    @Test
    void saveWhenPostWrite() {
        // given
        Set<String> input = of("java", "spring", "aws");
        Set<Hashtag> existInDB = of(createHashtag("java"), createHashtag("spring"), createHashtag("aws"));
        given(hashtagRepository.findByHashtagNameIn(input)).willReturn(existInDB);

        // when
        List<PostHashtag> beforePostHashtags = new ArrayList<>(post.getPostHashtags());
        sut.saveNewHashtagsWhenPostWrite(post, input);
        List<PostHashtag> afterPostHashtags = post.getPostHashtags();

        // then
        assertThat(beforePostHashtags).isEmpty();
        assertThat(afterPostHashtags).hasSize(input.size());
        then(hashtagRepository).should().saveAll(of());
    }

    @DisplayName("post 작성시 새롭게 저장할 해시태그들을 저장한다.")
    @MethodSource
    @ParameterizedTest
    void saveWhenPostWrite2(Set<String> input, Set<Hashtag> existingDB, Set<Hashtag> expected) {
        // given
        given(hashtagRepository.findByHashtagNameIn(input)).willReturn(existingDB);

        // when
        int beforePostHashtagSize = post.getPostHashtags().size();
        sut.saveNewHashtagsWhenPostWrite(post, input);
        int afterPostHashtagSize = post.getPostHashtags().size();

        // then
        assertThat(beforePostHashtagSize).isNotEqualTo(afterPostHashtagSize);
        then(hashtagRepository).should().saveAll(expected);
    }

    static Stream<Arguments> saveWhenPostWrite2() {
        return Stream.of(
                arguments(
                        of("java", "spring", "aws"),
                        of(createHashtag("java"), createHashtag("spring")),
                        of(createHashtag("aws"))
                ),
                arguments(
                        of("java","spring55"),
                        of(createHashtag("java"), createHashtag("spring")),
                        of(createHashtag("spring55"))
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
        sut.saveHashtagsWhenPostUpdate(post, input);
        int afterPostHashtagSize = post.getPostHashtags().size();

        // then
        assertThat(beforePostHashtagSize).isNotEqualTo(afterPostHashtagSize);
        then(hashtagRepository).should().saveAll(saveExpected);
    }


    static Stream<Arguments> saveNewHashtagsWhenPostUpdate() {
        return Stream.of(
                arguments(
                        of("java", "spring", "aws"),
                        of(createHashtag("java"), createHashtag("spring")),
                        of(createHashtag("aws"))
                ),
                arguments(
                        of("java","spring55"),
                        of(createHashtag("java"), createHashtag("spring")),
                        of(createHashtag("spring55"))
                )
        );
    }

    @DisplayName("글 수정시 기존 해시태그가 삭제된다")
    @MethodSource
    @ParameterizedTest
    void deleteNewHashtagsWhenPostUpdate(Set<String> input,Set<Hashtag> DBHashtag, Set<Hashtag> saveExpected) {
        // given
        Hashtag hashtag1 = createHashtag("java");
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
                        of(),
                        of(),
                        of(createHashtag("java"))
                )
        );
    }
}