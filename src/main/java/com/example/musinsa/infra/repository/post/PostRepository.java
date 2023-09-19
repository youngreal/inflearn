package com.example.musinsa.infra.repository.post;

import com.example.musinsa.domain.post.domain.Post;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    List<Post> findByTitleOrContentsContaining(String title, String contents, Pageable pageable);
}
