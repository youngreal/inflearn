package com.example.inflearn.service.post;

import static com.example.inflearn.domain.hashtag.Hashtag.createHashtag;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.inflearn.domain.hashtag.Hashtag;
import com.example.inflearn.service.hashtag.PostHashtagHandler;
import com.example.inflearn.domain.post.domain.PostHashtag;
import com.example.inflearn.service.hashtag.HashtagService;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.repository.post.HashtagRepository;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class HashtagServiceTest {

    @InjectMocks
    private HashtagService sut;

    @Mock
    private PostHashtagHandler postHashtagHandler;

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

    @Test
    void 게시글_작성시_새롭게_저장할_해시태그를_저장한다() {
        // given
        var inputHashtags = Set.of("java", "spring", "aws");
        var existInDB = Set.of(createHashtag("java"), createHashtag("spring"), createHashtag("aws"));
        given(hashtagRepository.findByHashtagNameIn(inputHashtags)).willReturn(existInDB);
        given(postHashtagHandler.hashtagsForInsert(post, convertToHashtags(inputHashtags), existInDB)).willReturn(
                Set.of());

        // when
        sut.saveHashtags(post, inputHashtags);

        // then
        then(hashtagRepository).should().saveAll(Set.of());
    }

    @Test
    void 글_수정시_새로운_해시태그가_추가된다() {
        // given
        var inputHashtags = Set.of("java", "spring", "aws");
        var existingDB = Set.of(createHashtag("java"), createHashtag("spring"));
        given(hashtagRepository.findByHashtagNameIn(inputHashtags)).willReturn(existingDB);
        given(postHashtagHandler.hashtagsWhenPostUpdate(post, convertToHashtags(inputHashtags), existingDB)).willReturn(
                Set.of(createHashtag("aws")));

        // when
        sut.saveHashtagsWhenPostUpdate(post, inputHashtags);

        // then
        then(hashtagRepository).should().saveAll(Set.of(createHashtag("aws")));
    }

    @Test
    void 글_수정시_기존_해시태그가_삭제된다() {
        // given
        Hashtag hashtag = createHashtag("java");
        PostHashtag postHashtag = PostHashtag.createPostHashtag(post, hashtag);
        post.addPostHashtag(postHashtag);
        hashtag.addPostHashtag(postHashtag);
        given(postHashtagHandler.hashtagsForDelete(post.getPostHashtags(), Set.of())).willReturn(Set.of(hashtag));

        // when
        sut.deleteHashtags(post.getPostHashtags(), Set.of());

        // then
        then(hashtagRepository).should().deleteAll(Set.of(hashtag));
    }

    private Set<Hashtag> convertToHashtags(Set<String> inputStringHashtags) {
        return inputStringHashtags.stream()
                .map(Hashtag::createHashtag)
                .collect(Collectors.toUnmodifiableSet());
    }
}
