package com.example.inflearn.service.hashtag;

import com.example.inflearn.domain.hashtag.Hashtag;
import com.example.inflearn.domain.post.domain.PostHashtag;
import com.example.inflearn.domain.post.domain.Post;
import com.example.inflearn.infra.repository.post.HashtagRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 해시태그를 입력받아 글작성/수정을 할때 이미 DB에있는 해시태그인지 아닌지 계산해보고 새로운해시태글만 해시태그 테이블에 insert해주는 클래스
 */
@Service
@RequiredArgsConstructor
public class HashtagService {

    private final PostHashtagHandler postHashtagHandler;
    private final HashtagRepository hashtagRepository;

    public void saveHashtags(Post post, Set<String> newHashtags) {
        Set<Hashtag> beforeHashtagsInDb = hashtagRepository.findByHashtagNameIn(newHashtags);
        hashtagRepository.saveAll(postHashtagHandler.hashtagsForInsert(post, convertToEntity(newHashtags), beforeHashtagsInDb));
    }

    public void saveHashtagsWhenPostUpdate(Post post, Set<String> newHashtags) {
        Set<Hashtag> beforeHashtagsInDb = hashtagRepository.findByHashtagNameIn(newHashtags);
        hashtagRepository.saveAll(postHashtagHandler.hashtagsWhenPostUpdate(post, convertToEntity(newHashtags), beforeHashtagsInDb)); // DB에 없던 요청받은 해시태그 삽입
    }

        //todo N+1문제 발생할수있음
    public void deleteHashtags(List<PostHashtag> beforePostHashtags, Set<String> inputStringHashtags) {
        hashtagRepository.deleteAll(
                postHashtagHandler.hashtagsForDelete(beforePostHashtags, convertToEntity(inputStringHashtags)));
    }

    private Set<Hashtag> convertToEntity(Set<String> inputStringHashtags) {
        return inputStringHashtags.stream()
                .map(Hashtag::createHashtag)
                .collect(Collectors.toUnmodifiableSet());
    }
}
