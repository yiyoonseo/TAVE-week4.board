package com.tave_week4.board.post.controller;

import com.tave_week4.board.post.dto.PostCreateRequest;
import com.tave_week4.board.post.dto.PostResponse;
import com.tave_week4.board.post.dto.PostSummaryResponse;
import com.tave_week4.board.post.dto.PostUpdateRequest;
import com.tave_week4.board.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody PostCreateRequest req) {
        Long id = postService.create(req);
        return ResponseEntity.created(URI.create("/api/posts/" + id)).build();
    }

    @GetMapping("/{id}")
    public PostResponse get(@PathVariable Long id) {
        return postService.getPost(id);
    }

    @GetMapping
    public Page<PostSummaryResponse> list(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return postService.getPosts(pageable);
    }

    @PutMapping("/{id}")
    public PostResponse get(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest req) {
        postService.update(id, req);
        return postService.getPost(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
