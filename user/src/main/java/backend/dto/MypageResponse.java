package backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MypageResponse {
    private String email;
    private String nickname;
    private boolean tokenIssued;
}
