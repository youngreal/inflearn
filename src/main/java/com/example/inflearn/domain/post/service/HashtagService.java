package com.example.inflearn.domain.post.service;

import com.example.inflearn.domain.Hashtag;
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

    public void saveNewHashtagsWhenPostWrite(Post post, Set<String> inputStringHashtags) {
        Set<Hashtag> existingHashtagsInDB = hashtagRepository.findByHashtagNameIn(inputStringHashtags); // 입력받은 문자열중 DB에 존재했던 해시태그들
        Set<Hashtag> inputHashtags = convertToHashtags(inputStringHashtags);
        Set<Hashtag> insertHashtags = createHashtagsForInsert(inputHashtags, existingHashtagsInDB);
        addPostHashtags(post, inputHashtags, insertHashtags, existingHashtagsInDB);

        //todo  동시상황에서 DataIntegrityViolationException 예외 발생할수있는데 try-catch할지? 아니면 핸들러로 넘길지?
        hashtagRepository.saveAll(insertHashtags);
    }

    public void saveHashtagsWhenPostUpdate(Post post, Set<String> inputStringHashtags) {
        Set<Hashtag> existingHashtagsInDB = hashtagRepository.findByHashtagNameIn(inputStringHashtags);
        Set<Hashtag> insertHashtags = getHashtagsForInsert(existingHashtagsInDB, convertToHashtags(inputStringHashtags));
        addPostHashtagsWhenPostUpdate(post, convertToHashtags(inputStringHashtags), insertHashtags, existingHashtagsInDB);

        hashtagRepository.saveAll(insertHashtags); // DB에 없던 요청받은 해시태그 삽입
    }

    public void deleteHashtags(List<PostHashtag> beforePostHashtags, Set<String> inputStringHashtags) {
        Set<Hashtag> hashtagsInPost = beforePostHashtags.stream()
                .map(PostHashtag::getHashtag)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());

        Set<Hashtag> deleteHashtags = getHashtagsForDelete(hashtagsInPost, convertToHashtags(inputStringHashtags)); // spring, aws
        hashtagRepository.deleteAll(deleteHashtags);
    }



    private Set<Hashtag> getHashtagsForInsert(Set<Hashtag> existingHashtagsInDB, Set<Hashtag> inputStringHashtags) {
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

    private Set<Hashtag> createHashtagsForInsert(Set<Hashtag> inputHashtags, Set<Hashtag> existingHashtagsInDB) {
        if (existingHashtagsInDB.isEmpty()) {
            return inputHashtags;
        } else {
            Set<Hashtag> hashtagsForInsert = new HashSet<>(inputHashtags);
            hashtagsForInsert.addAll(existingHashtagsInDB);
            hashtagsForInsert.removeAll(existingHashtagsInDB);

            return hashtagsForInsert;
        }
    }

    private void addPostHashtags(Post post, Set<Hashtag> inputHashtags,
            Set<Hashtag> insertHashtags, Set<Hashtag> existingHashtagsInDB) {

        // 새로 삽입해야하는 해시태그가 없는경우
        if (insertHashtags.isEmpty()) {
            // 작성요청시 해시태그 입력 안한경우
            if (inputHashtags.isEmpty()) {
                PostHashtag postHashtag = PostHashtag.createPostHashtag(post, null);
                post.addPostHashtag(postHashtag);
            }

            // 작성요청시 해시태그 입력받았지만 이미 DB에 있는 해시태그 사용하는경우
            for (Hashtag hashtagInDB : existingHashtagsInDB) {
                PostHashtag postHashtag = PostHashtag.createPostHashtag(post, hashtagInDB);
                post.addPostHashtag(postHashtag);
                hashtagInDB.addPostHashtag(postHashtag);
            }
        } else {
            // 새로 삽입해야하는 해시태그가 있는경우, DB에 있는 해시태그들은 먼저 기존에있던걸로 생성한다.
            for (Hashtag hashtagInDB : existingHashtagsInDB) {
                PostHashtag postHashtag = PostHashtag.createPostHashtag(post, hashtagInDB);
                post.addPostHashtag(postHashtag);
                hashtagInDB.addPostHashtag(postHashtag);
            }

            // 새로 삽입해야하는 해시태그들을 생성한다. DB에 없는 해시태그들은 새롭게 생성한다.
            for (Hashtag insertHashtag : insertHashtags) {
                PostHashtag postHashtag = PostHashtag.createPostHashtag(post, insertHashtag);
                post.addPostHashtag(postHashtag);
                postHashtag.getHashtag().addPostHashtag(postHashtag);
            }
        }
    }

    private void addPostHashtagsWhenPostUpdate(Post post, Set<Hashtag> inputHashtags, Set<Hashtag> insertToDBHashtags, Set<Hashtag> existingHashtagsInDB) {
        List<PostHashtag> beforePostHashtags = post.getPostHashtags();
        beforePostHashtags.clear();

        // DB에 새로넣을 해시태그가 존재하지 않는경우
        if (insertToDBHashtags.isEmpty()) {
            for (Hashtag inputHashtag : inputHashtags) {
                if (existingHashtagsInDB.contains(inputHashtag)) {

                    Hashtag hashtag = existingHashtagsInDB.stream()
                            .filter(existInDBHashtag -> existInDBHashtag.equals(inputHashtag))
                            .findAny()
                            .orElseThrow();

                    PostHashtag postHashtag = PostHashtag.createPostHashtag(post, hashtag);
                    post.addPostHashtag(postHashtag);
                    hashtag.addPostHashtag(postHashtag);
                }
            }

            // 해시태그가 아무것도 추가되지않은경우 null인 PostHashtag 생성
            if (post.getPostHashtags().isEmpty()) {
                post.addPostHashtag(PostHashtag.createPostHashtag(post, null));
            }

        }
        // DB에 새로넣을 해시태그가 존재하는경우
        else {
            for (Hashtag insertToDBHashtag : insertToDBHashtags) {
                PostHashtag postHashtag = PostHashtag.createPostHashtag(post, insertToDBHashtag);
                post.addPostHashtag(postHashtag);
                insertToDBHashtag.addPostHashtag(postHashtag);
            }
        }
    }

    private Set<Hashtag> convertToHashtags(Set<String> inputStringHashtags) {
        return inputStringHashtags.stream()
                .map(Hashtag::createHashtag)
                .collect(Collectors.toUnmodifiableSet());
    }
}
