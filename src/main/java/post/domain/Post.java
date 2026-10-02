package post.domain;

import global.entity.BaseItemEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
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
