package com.example.inflearn.domain.hashtag.service;

import com.example.inflearn.domain.hashtag.domain.Hashtag;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.domain.post.domain.PostHashtag;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/*
DB에 새로넣을 해시태그를 계산해주고, post와 hashtag에 postHashtag를 set해주는 역할
HashtagService에서 전부 이 로직을 실행하면 테스트하기가 어려워져서 분리를 시도했다.
 */

@Component
public class PostHashtagHandler {

    public Set<Hashtag> hashtagsForInsert(Post post, Set<Hashtag> inputHashtags, Set<Hashtag> existingHashtagsInDB) {
        Set<Hashtag> hashtagsForInsert = calculateHashtagsForInsert(inputHashtags, existingHashtagsInDB);
        //todo 어떻게 테스트하지?
        addPostHashtagToPostAndHashtag(post, existingHashtagsInDB, hashtagsForInsert);
        return hashtagsForInsert;
    }

    public Set<Hashtag> hashtagsWhenPostUpdate(Post post, Set<Hashtag> inputStringHashtags, Set<Hashtag> existingHashtagsInDB) {
        Set<Hashtag> hashtagsForInsert = calculateHashtagsForInsert(inputStringHashtags, existingHashtagsInDB);
        addPostHashtagsWhenPostUpdate(post, inputStringHashtags, hashtagsForInsert, existingHashtagsInDB);
        return hashtagsForInsert;
    }

    public Set<Hashtag> hashtagsForDelete(List<PostHashtag> beforePostHashtags, Set<Hashtag> inputStringHashtags) {
        Set<Hashtag> hashtagsInPost = beforePostHashtags.stream()
                .map(PostHashtag::getHashtag)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());

        return getHashtagsForDelete(hashtagsInPost, inputStringHashtags);
    }

    private Set<Hashtag> calculateHashtagsForInsert(Set<Hashtag> inputHashtags, Set<Hashtag> existingHashtagsInDB) {
        Set<Hashtag> hashtagsForInsert = new HashSet<>(inputHashtags);
        hashtagsForInsert.removeAll(existingHashtagsInDB);
        return hashtagsForInsert;
    }

    private void addPostHashtagToPostAndHashtag(Post post, Set<Hashtag> existingHashtagsInDB, Set<Hashtag> insertHashtags) {
        existingHashtagsInDB.forEach(hashtag -> createPostHashtag(post, hashtag));
        insertHashtags.forEach(hashtag -> createPostHashtag(post, hashtag));
    }

    private void createPostHashtag(Post post, Hashtag hashtag) {
        PostHashtag postHashtag = PostHashtag.createPostHashtag(post, hashtag);
        post.addPostHashtag(postHashtag);
        hashtag.addPostHashtag(postHashtag);
    }

    private void addPostHashtagsWhenPostUpdate(Post post, Set<Hashtag> inputHashtags, Set<Hashtag> insertToDBHashtags, Set<Hashtag> existingHashtagsInDB) {
        post.getPostHashtags().clear();
        inputHashtags.stream()
                .filter(existingHashtagsInDB::contains)
                .forEach(hashtag -> createPostHashtag(post, findExistingHashtag(existingHashtagsInDB, hashtag)));

        insertToDBHashtags.forEach(hashtag -> createPostHashtag(post,hashtag));
    }

    private Hashtag findExistingHashtag(Set<Hashtag> existingHashtagsInDB, Hashtag inputHashtag) {
        return existingHashtagsInDB.stream()
                .filter(existInDBHashtag -> existInDBHashtag.equals(inputHashtag))
                .findAny()
                .orElseThrow();
    }

    private Set<Hashtag> getHashtagsForDelete(Set<Hashtag> hashtagsInPost, Set<Hashtag> inputHashtags) {
        Set<Hashtag> hashtagsForDelete = new HashSet<>(hashtagsInPost);
        hashtagsForDelete.addAll(inputHashtags);
        hashtagsForDelete.removeAll(inputHashtags);

        return hashtagsForDelete.stream()
                .filter(Hashtag::hasOnlyOnePostHashtag)
                .collect(Collectors.toUnmodifiableSet());
    }
}
