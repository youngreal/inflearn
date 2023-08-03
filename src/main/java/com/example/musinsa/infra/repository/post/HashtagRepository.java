package com.example.musinsa.infra.repository.post;

import com.example.musinsa.domain.Hashtag;
import com.example.musinsa.domain.PostHashtag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    List<Hashtag> findByHashtagNameIn(List<String> hashtags);
}
