package com.example.musinsa.domain.post.service;

import com.example.musinsa.domain.Hashtag;
import com.example.musinsa.domain.PostHashtag;
import com.example.musinsa.domain.post.domain.Post;
import com.example.musinsa.infra.repository.post.HashtagRepository;
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

    public void saveNewHashtagsWhenPostWrite(Set<String> inputStringHashtags, Post post) {
        Set<Hashtag> existingHashtagsInDB = hashtagRepository.findByHashtagNameIn(inputStringHashtags); // 입력받은 문자열중 DB에 존재했던 해시태그들
        Set<Hashtag> madeHashtags = createHashtags(inputStringHashtags, existingHashtagsInDB);
        addPostHashtagsFromPostAndHashtag(post, madeHashtags);

        hashtagRepository.saveAll(madeHashtags);
    }

    public void saveNewHashtagsWhenPostUpdate(Post post, Set<String> inputStringHashtags) {
        Set<Hashtag> hashtagsInDB = hashtagRepository.findByHashtagNameIn(inputStringHashtags);
        Set<Hashtag> insertHashtags = getHashtagsForInsert(hashtagsInDB, createHashtag(inputStringHashtags));
        addPostHashtagsFromPostAndHashtag(post, insertHashtags);

        hashtagRepository.saveAll(insertHashtags); // DB에 없던 요청받은 해시태그 삽입
    }

    public void deleteHashtags(List<PostHashtag> postHashtags, Set<String> inputStringHashtags) {
        Set<Hashtag> hashtagsInPost = postHashtags.stream()
                .map(PostHashtag::getHashtag)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());

        hashtagRepository.deleteAll(getHashtagsForDelete(hashtagsInPost, stringToHashtag(inputStringHashtags)));
    }

    private Set<Hashtag> getHashtagsForInsert(Set<Hashtag> hashtagsInDB, Set<Hashtag> dtoHashtags) {
        Set<Hashtag> hashtagsForInsert = new HashSet<>(dtoHashtags);
        hashtagsForInsert.addAll(hashtagsInDB);
        hashtagsForInsert.removeAll(hashtagsInDB);

        return hashtagsForInsert.stream()
                .map(Hashtag::getHashtagName)
                .map(Hashtag::createHashtag)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<Hashtag> getHashtagsForDelete(Set<Hashtag> allHashtagsInDbSet, Set<Hashtag> dtoHashtags) {
        Set<Hashtag> hashtagsForDelete = new HashSet<>(allHashtagsInDbSet);
        hashtagsForDelete.addAll(dtoHashtags);
        hashtagsForDelete.removeAll(dtoHashtags);

        hashtagsForDelete.stream()
                .filter(Hashtag::hasOnlyOnePostHashtag)
                .forEach(Hashtag::deletePostHashtags);
        return hashtagsForDelete;
    }

    private Set<Hashtag> createHashtags(Set<String> inputStringHashtags, Set<Hashtag> existingHashtagsInDB) {
        if (existingHashtagsInDB.isEmpty()) {
            return createHashtag(inputStringHashtags);
        } else {
            return inputStringHashtags.stream()
                    .filter(inputStringHashtag -> existingHashtagsInDB.stream()
                            .map(Hashtag::getHashtagName)
                            .noneMatch(found -> found.equals(inputStringHashtag)))
                    .map(Hashtag::createHashtag)
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    private Set<Hashtag> createHashtag(Set<String> inputStringHashtags) {
        return inputStringHashtags.stream()
                .map(Hashtag::createHashtag)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * hashtag 에서 postHashtag 추가
     * post에서 postHashtag 추가
     */
    private void addPostHashtagsFromPostAndHashtag(Post post, Set<Hashtag> madeHashtags) {
        madeHashtags.stream()
                .map(hashtag -> PostHashtag.createPostHashtag(post, hashtag))
                .forEach(postHashtag -> {
                    post.addPostHashtag(postHashtag);
                    postHashtag.getHashtag().addPostHashtag(postHashtag);
                });
    }

    private Set<Hashtag> stringToHashtag(Set<String> dtoHashtagsString) {
        return new HashSet<>(createHashtag(dtoHashtagsString));
    }
}
