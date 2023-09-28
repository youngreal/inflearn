package com.example.inflearn.infra.repository.post;

import com.example.inflearn.domain.post.domain.Post;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface PostRepositoryCustom {

    List<Post> getPostsPerPage(int size, int page);

    List<Post> totalCount();
}
