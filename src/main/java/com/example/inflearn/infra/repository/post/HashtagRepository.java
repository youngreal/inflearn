package com.example.inflearn.infra.repository.post;

import com.example.inflearn.domain.Hashtag;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    //todo 성능 테스트
    Set<Hashtag> findByHashtagNameIn(Set<String> hashtags);
}
