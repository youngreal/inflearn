package com.example.musinsa.infra.repository.post;

import com.example.musinsa.domain.post.domain.Post;
import java.util.List;

public interface PostRepositoryCustom {

    List<Post> getPostsPerPage(int size, int page);

    List<Post> totalCount();
}
