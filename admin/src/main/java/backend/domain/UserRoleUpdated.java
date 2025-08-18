package backend.domain;

import backend.infra.AbstractEvent;
import lombok.Data;

@Data
public class UserRoleUpdated extends AbstractEvent{
    private Long userId;
    private RoleType role;
}
