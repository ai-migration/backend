package backend.domain;

import backend.infra.AbstractEvent;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class ConversionRequested extends AbstractEvent {
    private Long jobId;

    private Long userId;

    private String filePath;

    private String inputeGovFrameVer;

    private String outputeGovFrameVer;

    private Boolean isTestCode;

    private String conversionType;

    public ConversionRequested(Agent aggregate) {
        super(aggregate);
    }

    public ConversionRequested() {
        super();
    }
}
//>>> DDD / Domain Event
