package backend.dto;

import backend.domain.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDto { // 프론트->백엔트 요청
    private String password;
    private String newPassword;
}
