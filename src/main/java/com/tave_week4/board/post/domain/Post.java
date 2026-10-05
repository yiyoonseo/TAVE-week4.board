package com.tave_week4.board.post.domain;

import com.tave_week4.board.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob @Column(nullable = false)
    private String content;

    @Column(nullable = false, length = 30)
    private String author;

    public Post(String author, String content, String title) {
        this.author = author;
        this.content = content;
        this.title = title;
    }

    public void update(String content, String title) {
        this.content = content;
        this.title = title;
    }

}
