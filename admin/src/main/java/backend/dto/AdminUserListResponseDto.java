package backend.dto;

import lombok.Data;

@Data
public class AdminUserListResponseDto {
    // readmodel
    private Long userId;
    private String email;
    private String nickname;
    private String role;
    private boolean tokenIssued;
}
