package com.example.inflearn.dto;

import java.util.ArrayList;
import java.util.List;

public record CommentDto(
        Long commentId,
        String parentCommentContent,
        List<ReplyDto> childComments
) {

    public static CommentDto create(Long commentId, String contents) {
        return new CommentDto(commentId, contents, new ArrayList<>());
    }

    public void addReply(Long parentCommentId, String contents) {
        if (this.commentId().equals(parentCommentId)) {
            this.childComments().add(ReplyDto.create(contents));
        }
    }
}
