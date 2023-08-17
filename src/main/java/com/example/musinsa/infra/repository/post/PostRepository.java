package com.example.musinsa.infra.repository.post;

import com.example.musinsa.domain.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByTitleOrContentsContaining(String title, String contents, Pageable pageable);
}
