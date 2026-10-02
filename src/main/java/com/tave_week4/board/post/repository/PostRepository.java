package com.tave_week4.board.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tave_week4.board.post.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
