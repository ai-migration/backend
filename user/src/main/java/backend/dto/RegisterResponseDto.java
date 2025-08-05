package backend.dto;

import backend.domain.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponseDto {
    private Long userId;
    private String email;
    private String nickname;
    private RoleType role;
}
