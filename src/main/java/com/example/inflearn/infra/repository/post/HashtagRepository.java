package com.example.inflearn.infra.repository.post;

import com.example.inflearn.domain.hashtag.Hashtag;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Set<Hashtag> findByHashtagNameIn(Set<String> hashtags);
}
