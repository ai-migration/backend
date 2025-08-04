package backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRegisterRequested {
    private Long postId;
    private String title;
    private String content;
    private PostType type;
}
