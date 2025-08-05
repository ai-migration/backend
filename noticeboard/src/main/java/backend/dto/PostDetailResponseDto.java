package backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;

import backend.domain.PostType;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDetailResponseDto {
    private Long postId;
    private String title;
    private String content;
    private PostType type;
    private Date createdAt;
    private Date updateAt;
    private int viewCount;
}
