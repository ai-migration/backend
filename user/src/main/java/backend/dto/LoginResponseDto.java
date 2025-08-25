package backend.dto;

import backend.domain.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto { // 백엔트->프론트 응답
    private String accessToken;
    private Long id;
    private String nickname;
    private RoleType role;
    private boolean isExpired;
}
