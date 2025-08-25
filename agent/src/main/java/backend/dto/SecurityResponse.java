package backend.dto;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SecurityResponse {
    // 파이썬이 보내는 camelCase 키에 맞춤
    private List<Map<String, Object>> agentInputs;
    private Map<String, Object> metrics;
    private Map<String, Object> qualityGate;
    private Map<String, Object> issues;                  // 리스트/맵 모두 허용
    private Object reportJson;
    private List<MarkdownFile> markdowns;
}