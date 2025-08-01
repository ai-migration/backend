package backend.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRegisterRequested{
    
    private Long postId;
    private String title;
    private String content;
    private PostType type;
    //private Long createdAt; 게시판 도메인에서 처리
    //private Long updatedAt; 업데이트 
    //private Long adminId; 나중에 어떤 관리자가 수정했는지 확인을 위하면 사용하는 것으로 생각
}
