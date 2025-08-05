package backend.dto;

import backend.domain.RoleType;
import lombok.Data;

@Data
public class AdminUserListResponseDto {
    // readmodel
    private Long userId;
    private String email;
    private String nickname;
    private RoleType role;
    private boolean tokenIssued;
}
