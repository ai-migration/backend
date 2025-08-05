package backend.dto;

import java.time.LocalDateTime;

import backend.domain.RoleType;
import lombok.Data;
import java.util.Date;


@Data
public class AdminUserDetailResponseDto {
    // readmodel
    private Long userId;
    private String email;
    private String nickname;
    private RoleType role;
    private boolean tokenIssued;

    // Token 정보
    private String apiKey;
    private boolean active;
    private Date createdAt;
}
