package backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MypageResponseDto {
    private String email;
    private String nickname;
    private boolean tokenIssued;
}
