package com.example.musinsa.infra.repository.post;

import com.example.musinsa.domain.post.domain.Post;
import java.util.List;

public interface PostRepositoryCustom {

    List<Post> getPostPerPage(int page, int pageNumber);

    List<Post> totalCount();
}
