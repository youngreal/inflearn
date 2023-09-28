package com.example.inflearn.infra.repository.post;

import com.example.inflearn.domain.post.domain.Post;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    List<Post> findByTitleContainingOrContentsContaining(String title, String contents, Pageable pageable);

    @Query(value = "select * from post where match(title,contents) against(?) limit 20", nativeQuery = true)
    List<Post> test(String searchWord);
}
