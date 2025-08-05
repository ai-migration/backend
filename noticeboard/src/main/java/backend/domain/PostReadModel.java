package backend.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

//<<< EDA / CQRS
@Entity
@Table(name = "PostReadModel_table")
@Data
public class PostReadModel {

    @Id
    //@GeneratedValue(strategy=GenerationType.AUTO)
    private Long postId;

    private String title;
    private String content;
    private PostType type;
    private Date createdAt;
    private int viewCount;
    private Date updatedAt;
}
