package backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostListFaqResponseDto {
    private Long postId;
    private String title;
    private String content;
}
