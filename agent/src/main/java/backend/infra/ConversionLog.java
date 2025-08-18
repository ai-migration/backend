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
@Document(collection = "conversion_logs")
public class ConversionLog {

    @Id
    private String id; // ✅ MongoDB에서 자동 생성 가능

    private Long jobId;
    private Long userId;
    private String inputLanguage;
    private String s3OriginPath;
    private Date savedAt;
    private List<String> s3ConvControllerPath;
    private List<String> s3ConvServicePath;
    private List<String> s3ConvServiceimplPath;
    private List<String> s3ConvVoPath;
    private List<Map<String, Object>> convControllerReport;
    private List<Map<String, Object>> convServiceReport;
    private List<Map<String, Object>> convServiceimplReport;
    private List<Map<String, Object>> convVoReport;

}
