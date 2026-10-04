package com.tave_week4.board.post.service;

import com.tave_week4.board.global.exception.PostNotFoundException;

import com.tave_week4.board.post.dto.PostSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tave_week4.board.post.domain.Post;
import com.tave_week4.board.post.dto.PostCreateRequest;
import com.tave_week4.board.post.dto.PostResponse;
import com.tave_week4.board.post.dto.PostUpdateRequest;
import com.tave_week4.board.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;

    @Transactional
    public Long create(PostCreateRequest req) {
        return postRepository.save(new Post(req.title(), req.content(), req.author())).getId();
    }

    // 단건 조회
    @Cacheable(cacheNames = "com/tave_week4/board/post", key = "#id")
    public PostResponse getPost(Long id) {
        return PostResponse.from(findPost(id));
    }

    // 목록 조회
    public Page<PostSummaryResponse> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(PostSummaryResponse::from);
    }

    @Transactional
    @CacheEvict(cacheNames = "com/tave_week4/board/post", key = "#id")
    public void update(Long id, PostUpdateRequest req) {
        findPost(id).update(req.title(), req.content());
    }

    @Transactional
    @CacheEvict(cacheNames = "com/tave_week4/board/post", key = "#id")
    public void delete(Long id) {
        postRepository.delete(findPost(id));
    }

    private Post findPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }
}
