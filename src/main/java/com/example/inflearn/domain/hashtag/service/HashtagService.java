package com.example.inflearn.domain.hashtag.service;

import com.example.inflearn.domain.hashtag.domain.Hashtag;
import com.example.inflearn.domain.PostHashtag;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    public void saveHashtags(Post post, Set<String> inputStringHashtags) {
        hashtagRepository.saveAll(getHashtagsForInsertWhenPostSave(post, inputStringHashtags));
    }

    public void saveHashtagsWhenPostUpdate(Post post, Set<String> inputStringHashtags) {
        Set<Hashtag> existingHashtagsInDB = hashtagRepository.findByHashtagNameIn(inputStringHashtags);
        Set<Hashtag> insertHashtags = getHashtagsForInsertWhenPostUpdate(existingHashtagsInDB, convertToHashtagType(inputStringHashtags));
        addPostHashtagsWhenPostUpdate(post, convertToHashtagType(inputStringHashtags), insertHashtags, existingHashtagsInDB);

        hashtagRepository.saveAll(insertHashtags); // DB에 없던 요청받은 해시태그 삽입
    }

    public void deleteHashtags(List<PostHashtag> beforePostHashtags, Set<String> inputStringHashtags) {
        Set<Hashtag> hashtagsInPost = beforePostHashtags.stream()
                .map(PostHashtag::getHashtag)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());

        //todo N+1문제 발생할수있음
        hashtagRepository.deleteAll(getHashtagsForDelete(hashtagsInPost, convertToHashtagType(inputStringHashtags)));
    }

    private Set<Hashtag> getHashtagsForInsertWhenPostUpdate(Set<Hashtag> existingHashtagsInDB, Set<Hashtag> inputStringHashtags) {
        Set<Hashtag> hashtagsForInsert = new HashSet<>(inputStringHashtags);
        hashtagsForInsert.addAll(existingHashtagsInDB);
        hashtagsForInsert.removeAll(existingHashtagsInDB);

        return hashtagsForInsert.stream()
                .map(Hashtag::getHashtagName)
                .map(Hashtag::createHashtag)
                .collect(Collectors.toUnmodifiableSet());
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
        if (existingHashtagsInDB.isEmpty()) {
            return inputHashtags;
        } else {
            //todo DB에있는 해시태그와 입력받은해시태그중 DB에 없는 해시태그를 필터링하는과정인데 해당방법이 최선일까?
            Set<Hashtag> hashtagsForInsert = new HashSet<>(inputHashtags);
            hashtagsForInsert.addAll(existingHashtagsInDB);
            hashtagsForInsert.removeAll(existingHashtagsInDB);

            return hashtagsForInsert;
        }
    }

    // DB에 넣을 해시태그가 비었으면
    private void addPostHashtagsWhenPostUpdate(Post post, Set<Hashtag> inputHashtags, Set<Hashtag> insertToDBHashtags, Set<Hashtag> existingHashtagsInDB) {
//        List<PostHashtag> beforePostHashtags = post.getPostHashtags();
//        beforePostHashtags.clear();

        // DB에 새로넣을 해시태그가 존재하지 않는경우
        if (insertToDBHashtags.isEmpty()) {
            for (Hashtag inputHashtag : inputHashtags) {
                if (existingHashtagsInDB.contains(inputHashtag)) {
                    Hashtag hashtag = existingHashtagsInDB.stream()
                            .filter(existInDBHashtag -> existInDBHashtag.equals(inputHashtag))
                            .findAny()
                            .orElseThrow();

                    createPostHashtag(post, hashtag);
                }
            }
        }
        // DB에 새로넣을 해시태그가 존재하는경우
        else {
            for (Hashtag hashtag : insertToDBHashtags) {
                createPostHashtag(post, hashtag);
            }
        }
    }

    private Set<Hashtag> convertToHashtagType(Set<String> inputStringHashtags) {
        return inputStringHashtags.stream()
                .map(Hashtag::createHashtag)
                .collect(Collectors.toUnmodifiableSet());
    }

    private void createPostHashtag(Post post, Hashtag hashtag) {
            PostHashtag postHashtag = PostHashtag.createPostHashtag(post, hashtag);
            post.addPostHashtag(postHashtag);
            hashtag.addPostHashtag(postHashtag);
    }

    private Set<Hashtag> getHashtagsForInsertWhenPostSave(Post post, Set<String> inputStringHashtags) {
        Set<Hashtag> existingHashtagsInDB = hashtagRepository.findByHashtagNameIn(inputStringHashtags); // 입력받은 문자열중 DB에 존재했던 해시태그들
        Set<Hashtag> insertHashtags = createHashtagsForInsert(convertToHashtagType(inputStringHashtags), existingHashtagsInDB);
        createPostHashtags(post, existingHashtagsInDB, insertHashtags);
        return insertHashtags;
    }

    private void createPostHashtags(Post post, Set<Hashtag> existingHashtagsInDB, Set<Hashtag> insertHashtags) {
        for (Hashtag hashtag : existingHashtagsInDB) {
            createPostHashtag(post, hashtag);
        }

        if (!insertHashtags.isEmpty()) {
            for (Hashtag hashtag : insertHashtags) {
                createPostHashtag(post, hashtag);
            }
        }
    }
}
