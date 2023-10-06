package com.example.inflearn.infra.repository.comment;

import com.example.inflearn.domain.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
