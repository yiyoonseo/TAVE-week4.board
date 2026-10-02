package com.tave_week4.board.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
        @NotBlank  @Size(max = 100)
        String title,

        @NotBlank
        String content,


        @NotBlank @Size(max = 30)
        String author) { }
