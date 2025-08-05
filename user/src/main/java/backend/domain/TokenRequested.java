package backend.domain;

import backend.domain.*;
import backend.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class TokenRequested extends AbstractEvent {

    private Long id;    
    private boolean tokenIssued;

    public TokenRequested(User aggregate) {
        super(aggregate);
        this.id = aggregate.getId();
        this.tokenIssued = aggregate.getTokenIssued();
    }

    public TokenRequested() {
        super();
    }
}
//>>> DDD / Domain Event
