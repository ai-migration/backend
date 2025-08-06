package backend.domain;

import backend.NoticeboardApplication;

import java.util.Date;
import javax.persistence.*;

import lombok.Data;

@Entity
@Table(name = "Post_table")
@Data
//<<< DDD / Aggregate Root
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long postId;

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private PostType type;

    @Column(updatable = false)
    private Date createdAt;

    private Date updatedAt;

    public static PostRepository repository() {
        PostRepository postRepository = NoticeboardApplication.applicationContext.getBean(
            PostRepository.class
        );
        return postRepository;
    }

}
//>>> DDD / Aggregate Root
