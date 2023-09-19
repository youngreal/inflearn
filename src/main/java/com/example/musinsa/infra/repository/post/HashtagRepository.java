package com.example.musinsa.infra.repository.post;

import com.example.musinsa.domain.Hashtag;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Set<Hashtag> findByHashtagNameIn(Set<String> hashtags);
}