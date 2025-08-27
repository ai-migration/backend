package backend.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentEvent {
    @JsonProperty("userId")   // camelCase 매핑
    private Long userId;
    @JsonProperty("jobId")
    private Long jobId;
    private String status;
    private String language;
    private String description;
    private ConversionResponse result;
}