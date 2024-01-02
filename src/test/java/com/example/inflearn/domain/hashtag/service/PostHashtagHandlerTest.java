package com.example.inflearn.domain.hashtag.service;

import static com.example.inflearn.domain.hashtag.domain.Hashtag.createHashtag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.example.inflearn.domain.hashtag.domain.Hashtag;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.domain.post.domain.PostHashtag;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PostHashtagHandlerTest {

    private PostHashtagHandler sut;
    private Post post;

    @BeforeEach
    void setup() {
        sut = new PostHashtagHandler();
    }

    @MethodSource
    @ParameterizedTest
    void 게시글_생성시_새로_저장할_해시태그를_계산한다(Set<Hashtag> inputHashtags, Set<Hashtag> existingDB, Set<Hashtag> newHashtagForInsert, int PostHashtagEntityCount) {
        // given
        post = createPost(new ArrayList<>());

        // when
        Set<Hashtag> actual = sut.hashtagsForInsert(post, inputHashtags, existingDB);

        // then
        assertThat(actual).isEqualTo(newHashtagForInsert);
        assertThat(post.getPostHashtags()).hasSize(PostHashtagEntityCount);
    }

    static Stream<Arguments> 게시글_생성시_새로_저장할_해시태그를_계산한다() {
        return Stream.of(
                arguments(
                        Set.of(createHashtag("java"),createHashtag("spring")),
                        Set.of(),
                        Set.of(createHashtag("java"), createHashtag("spring")),
                        2
                ),
                arguments(
                        Set.of(createHashtag("java")),
                        Set.of(),
                        Set.of(createHashtag("java")),
                        1
                ),
                arguments(
                        Set.of(createHashtag("java"), createHashtag("spring55")),
                        Set.of(createHashtag("spring55")),
                        Set.of(createHashtag("java")),
                        2
                ),
                arguments(
                        Set.of(createHashtag("java"), createHashtag("spring55")),
                        Set.of(createHashtag("java"), createHashtag("spring55")),
                        Set.of(),
                        2
                ),
                arguments(
                        Set.of(),
                        Set.of(),
                        Set.of(),
                        0
                )
        );
    }

    @MethodSource
    @ParameterizedTest
    void 게시글_수정시_새로_저장할_해시태그를_계산한다(Set<Hashtag> inputHashtags, Set<Hashtag> existingDB, Set<Hashtag> newHashtagForInsert, int PostHashtagEntityCount) {
        // given
        post = createPost(new ArrayList<>(List.of(PostHashtag.createPostHashtag(Post.builder().build(), createHashtag("java")))));

        // when
        Set<Hashtag> actual = sut.hashtagsWhenPostUpdate(post, inputHashtags, existingDB);

        // then
        assertThat(actual).isEqualTo(newHashtagForInsert);
        assertThat(post.getPostHashtags()).hasSize(PostHashtagEntityCount);
    }

    static Stream<Arguments> 게시글_수정시_새로_저장할_해시태그를_계산한다() {
        return Stream.of(
                arguments(
                        Set.of(createHashtag("java"),createHashtag("spring")),
                        Set.of(createHashtag("java")),
                        Set.of(createHashtag("spring")),
                        2
                ),
                arguments(
                        Set.of(createHashtag("java")),
                        Set.of(createHashtag("java")),
                        Set.of(),
                        1
                ),
                arguments(
                        Set.of(createHashtag("java"), createHashtag("spring55")),
                        Set.of(createHashtag("java")),
                        Set.of(createHashtag("spring55")),
                        2
                ),
                arguments(
                        Set.of(),
                        Set.of(),
                        Set.of(),
                        0
                )
        );
    }

    @MethodSource
    @ParameterizedTest
    void 게시글_수정시_삭제할_해시태그를_삭제한다(Set<Hashtag> inputHashtags, Set<Hashtag> hashtagsForDelete) {
        // given
        post = createPost(new ArrayList<>(
                List.of(PostHashtag.createPostHashtag(Post.builder().build(),
                        createHashtag("java")))));

        // when
        Set<Hashtag> actual = sut.hashtagsForDelete(post.getPostHashtags(), inputHashtags);

        // then
        assertThat(actual).isEqualTo(hashtagsForDelete);
    }

    static Stream<Arguments> 게시글_수정시_삭제할_해시태그를_삭제한다() {
        return Stream.of(
                arguments(
                        Set.of(createHashtag("java"), createHashtag("spring")),
                        Set.of()
                ),
                arguments(
                        Set.of(createHashtag("java")),
                        Set.of()
                )
        );
    }

    private Post createPost(ArrayList<PostHashtag> postHashtags) {
        return Post.builder()
                .title("제목1")
                .contents("내용1")
                .postHashtags(postHashtags)
                .build();
    }
}