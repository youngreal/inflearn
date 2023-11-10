package com.example.inflearn.domain.hashtag.service;

import com.example.inflearn.domain.hashtag.domain.Hashtag;
import com.example.inflearn.domain.post.domain.PostHashtag;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.repository.post.HashtagRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HashtagService {

    private final PostHashtagHandler postHashtagHandler;
    private final HashtagRepository hashtagRepository;

    public void saveHashtags(Post post, Set<String> inputStringHashtags) {
        Set<Hashtag> existingHashtagsInDB = hashtagRepository.findByHashtagNameIn(inputStringHashtags);
        hashtagRepository.saveAll(postHashtagHandler.hashtagsForInsert(post, convertToHashtags(inputStringHashtags), existingHashtagsInDB));
    }

    public void saveHashtagsWhenPostUpdate(Post post, Set<String> inputStringHashtags) {
        Set<Hashtag> existingHashtagsInDB = hashtagRepository.findByHashtagNameIn(inputStringHashtags);
        hashtagRepository.saveAll(postHashtagHandler.hashtagsWhenPostUpdate(post, convertToHashtags(inputStringHashtags), existingHashtagsInDB)); // DB에 없던 요청받은 해시태그 삽입
    }

    public void deleteHashtags(List<PostHashtag> beforePostHashtags, Set<String> inputStringHashtags) {
        //todo N+1문제 발생할수있음
        hashtagRepository.deleteAll(
                postHashtagHandler.hashtagsForDelete(beforePostHashtags, convertToHashtags(inputStringHashtags)));
    }

    private Set<Hashtag> convertToHashtags(Set<String> inputStringHashtags) {
        return inputStringHashtags.stream()
                .map(Hashtag::createHashtag)
                .collect(Collectors.toUnmodifiableSet());
    }
}
