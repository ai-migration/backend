package backend.dto;

import backend.domain.RoleType;
import lombok.Data;

@Data
public class UpdateUserRoleRequestDto {
    private RoleType role;
}
