package backend.domain;

import backend.domain.*;
import backend.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class UserRegistered extends AbstractEvent {

    private Long id;
    private String email;
    // private String password;
    private String nickname;
    private RoleType role;
    private Boolean tokenIssued;

    public UserRegistered(User aggregate) {
        super(aggregate);
        this.id = aggregate.getId();
        this.email = aggregate.getEmail();
        this.nickname = aggregate.getNickname();
        this.role = aggregate.getRole();
        this.tokenIssued = false;  // ✅ 여기서 강제 고정!
    }

    public UserRegistered() {
        super();
    }
}
//>>> DDD / Domain Event
