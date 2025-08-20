package backend.infra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.util.Date;
import java.util.List;
import java.util.Map;
@Data
@Document(collection = "security_logs")
public class SecurityLog {

    @Id
    private String id;

    private Long jobId;
    private Long userId;
    
    // 입력 zip
    private String s3OriginPath;       // 업로드된 ZIP S3 Key
    private Date savedAt;              // 파이프라인 종료 시각 (성공/실패 시 세팅)
    
    private String s3AgentInputsPath;  // e.g. {userId}/{jobId}/outputs/agent_inputs.json
    private String s3ReportsDir;       // 예: {userId}/{jobId}/outputs/security_reports/
    private String s3ReportJsonPath;   // 예: .../outputs/security_reports/report.json
    private List<String> issueReportFiles; // 개별 MD 파일 Key 목록
    private Integer issueCount;        // report.json 기준 이슈 건수 요약
    private List<Map<String, Object>> securityReport; 
}
