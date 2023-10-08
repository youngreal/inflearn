package com.example.inflearn.dto;

public record ReplyDto(
        String childCommentContent
) {

    public static ReplyDto create(String contents) {
        return new ReplyDto(contents);
    }
}
