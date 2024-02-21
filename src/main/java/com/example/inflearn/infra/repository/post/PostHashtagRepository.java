package com.example.inflearn.infra.repository.post;

import com.example.inflearn.domain.post.domain.PostHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long> {

}
