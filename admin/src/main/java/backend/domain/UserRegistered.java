package backend.domain;

import backend.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class UserRegistered extends AbstractEvent {

    private Long id;
    private String email;
    private String nickname;
    private RoleType role;
    private Boolean tokenIssued;
}
