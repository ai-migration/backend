package backend.domain;

import backend.infra.AbstractEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequested extends AbstractEvent{
    private Long postId;
    private String title;
    private String content;
    private PostType type;
    //private Date createdAt; 필요x
    //private Date updatedAt; 게시판 도메인에서 처리
    //private Long adminId; 나중에 어떤 관리자가 수정했는지 확인을 위하면 사용하는 것으로 생각

}
