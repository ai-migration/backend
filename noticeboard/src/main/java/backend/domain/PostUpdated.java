package backend.domain;

import backend.domain.*;
import backend.infra.AbstractEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class PostUpdated extends AbstractEvent {

    private Long postId;             
    private String title;            
    private String content;          
    private PostType type;           
    private Date updatedAt; 

    public PostUpdated(Post aggregate) {
        super(aggregate);
    }

    public PostUpdated() {
        super();
    }
}
//>>> DDD / Domain Event
