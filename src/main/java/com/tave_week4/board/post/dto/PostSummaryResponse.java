package com.tave_week4.board.post.dto;

import com.tave_week4.board.post.domain.Post;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        String title,
        String author,
        LocalDateTime createdAt
) {
    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getAuthor(),
                post.getCreatedAt()
        );
    }
}
