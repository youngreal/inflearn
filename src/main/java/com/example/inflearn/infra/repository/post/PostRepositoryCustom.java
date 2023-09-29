package com.example.inflearn.infra.repository.post;

import com.example.inflearn.domain.post.domain.Post;
import java.util.List;

public interface PostRepositoryCustom {

    List<Post> getPostsPerPage(int page, int size);
}
