package backend.dto;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SecurityAgentEvent {
    private String eventType;        // "SecurityFinished"
    private Long userId;
    private Long jobId;
    private String status;
    private Integer exitCode;
    private String projectKey;
    private String projectRootPath;
    private String projectRootName;
    private String outputsDir;
    private List<Map<String, String>> checkpoints;

    private SecurityResponse result; 
}