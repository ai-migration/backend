package backend.dto;


import lombok.Data;

@Data
public class AgentEvent {
    private Long userId;
    private Long jobId;
    private String status;
    private String language;
    private String description;
}