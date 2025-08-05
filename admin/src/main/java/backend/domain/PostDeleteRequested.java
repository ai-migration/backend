package backend.domain;

import backend.infra.AbstractEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDeleteRequested extends AbstractEvent{
    private Long postId;
    //private Long adminId; 나중에 어떤 관리자가 수정했는지 확인을 위하면 사용하는 것으로 생각

}
