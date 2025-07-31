package backend.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AdminUserDetailResponseDto {
    // readmodel
    private Long userId;
    private String email;
    private String nickname;
    private String role;
    private boolean tokenIssued;

    // Token 정보
    private String apiKey;
    private boolean active;
    private LocalDateTime createdAt;
}
