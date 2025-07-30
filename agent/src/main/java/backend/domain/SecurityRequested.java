package backend.domain;

import backend.domain.*;
import backend.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class SecurityRequested extends AbstractEvent {

    private Long userId;

    private String filePath;

    public SecurityRequested(Agent aggregate) {
        super(aggregate);
    }

    public SecurityRequested() {
        super();
    }
}
//>>> DDD / Domain Event
