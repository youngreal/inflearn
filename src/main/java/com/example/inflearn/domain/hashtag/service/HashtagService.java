package com.example.inflearn.domain.hashtag.service;

import com.example.inflearn.domain.hashtag.domain.Hashtag;
import com.example.inflearn.domain.post.domain.PostHashtag;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.repository.post.HashtagRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

//todo 이 클래스는 아무리 리팩토링을해도 테스트하기도 쉽지않고 의도파악도 쉬운편은 아닌것같다. 어떻게 개선해볼지 생각해보자
@Slf4j
@Service
@RequiredArgsConstructor
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    public void saveHashtags(Post post, Set<String> inputStringHashtags) {
        Set<Hashtag> existingHashtagsInDB = hashtagRepository.findByHashtagNameIn(inputStringHashtags);
        Set<Hashtag> insertHashtags = createHashtagsForInsert(convertToHashtags(inputStringHashtags), existingHashtagsInDB);
        createPostHashtags(post, existingHashtagsInDB, insertHashtags);

        hashtagRepository.saveAll(insertHashtags);
    }

    public void saveHashtagsWhenPostUpdate(Post post, Set<String> inputStringHashtags) {
        Set<Hashtag> existingHashtagsInDB = hashtagRepository.findByHashtagNameIn(inputStringHashtags);
        Set<Hashtag> insertHashtags = getHashtagsForInsertWhenPostUpdate(existingHashtagsInDB, convertToHashtags(inputStringHashtags));
        addPostHashtagsWhenPostUpdate(post, convertToHashtags(inputStringHashtags), insertHashtags, existingHashtagsInDB);

        hashtagRepository.saveAll(insertHashtags); // DB에 없던 요청받은 해시태그 삽입
    }

    public void deleteHashtags(List<PostHashtag> beforePostHashtags, Set<String> inputStringHashtags) {
        Set<Hashtag> hashtagsInPost = beforePostHashtags.stream()
                .map(PostHashtag::getHashtag)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());

        //todo N+1문제 발생할수있음
        hashtagRepository.deleteAll(getHashtagsForDelete(hashtagsInPost, convertToHashtags(inputStringHashtags)));
    }

    private Set<Hashtag> getHashtagsForInsertWhenPostUpdate(Set<Hashtag> existingHashtagsInDB, Set<Hashtag> inputStringHashtags) {
        Set<Hashtag> hashtagsForInsert = new HashSet<>(inputStringHashtags);
        hashtagsForInsert.removeAll(existingHashtagsInDB);
        return hashtagsForInsert;
    }

    private Set<Hashtag> getHashtagsForDelete(Set<Hashtag> hashtagsInPost, Set<Hashtag> inputHashtags) {
        Set<Hashtag> hashtagsForDelete = new HashSet<>(hashtagsInPost);
        hashtagsForDelete.addAll(inputHashtags);
        hashtagsForDelete.removeAll(inputHashtags);

        return hashtagsForDelete.stream()
                .filter(Hashtag::hasOnlyOnePostHashtag)
                .collect(Collectors.toUnmodifiableSet());
    }

    // DB에 존재하는게 비었으면 input해시태그를, DB에 존재하는게 있으면 input에서 DB에있는걸 배제한 애만 DB에 넣는다
    private Set<Hashtag> createHashtagsForInsert(Set<Hashtag> inputHashtags, Set<Hashtag> existingHashtagsInDB) {
        Set<Hashtag> hashtagsForInsert = new HashSet<>(inputHashtags);
        hashtagsForInsert.removeAll(existingHashtagsInDB);
        return hashtagsForInsert;
    }

    // DB에 넣을 해시태그가 비었으면
    private void addPostHashtagsWhenPostUpdate(Post post, Set<Hashtag> inputHashtags, Set<Hashtag> insertToDBHashtags, Set<Hashtag> existingHashtagsInDB) {
        // DB에 새로넣을 해시태그가 존재하지 않는경우
        for (Hashtag inputHashtag : inputHashtags) {
            if (existingHashtagsInDB.contains(inputHashtag)) {
                Hashtag hashtag = existingHashtagsInDB.stream()
                        .filter(existInDBHashtag -> existInDBHashtag.equals(inputHashtag))
                        .findAny()
                        .orElseThrow();

                createPostHashtag(post, hashtag);
            }
        }
        // DB에 새로넣을 해시태그가 존재하는경우
        for (Hashtag hashtag : insertToDBHashtags) {
            createPostHashtag(post, hashtag);
        }
    }

    private Set<Hashtag> convertToHashtags(Set<String> inputStringHashtags) {
        return inputStringHashtags.stream()
                .map(Hashtag::createHashtag)
                .collect(Collectors.toUnmodifiableSet());
    }

    private void createPostHashtag(Post post, Hashtag hashtag) {
        PostHashtag postHashtag = PostHashtag.createPostHashtag(post, hashtag);
        post.addPostHashtag(postHashtag);
        hashtag.addPostHashtag(postHashtag);
    }

    private void createPostHashtags(Post post, Set<Hashtag> existingHashtagsInDB, Set<Hashtag> insertHashtags) {
        existingHashtagsInDB.forEach(hashtag -> createPostHashtag(post, hashtag));
        insertHashtags.forEach(hashtag -> createPostHashtag(post, hashtag));
    }
}
