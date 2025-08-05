package backend.dto;

import java.time.LocalDateTime;
import java.util.Date;

import backend.domain.PostType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostListNoticeResponseDto {
    private Long postId;
    private String title;
    private PostType type;
    private Date createdAt;
    private int viewCount;
}
