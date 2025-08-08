package backend.infra;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.util.Date;

@Data
@Document(collection = "conversion_logs")
public class ConversionLog {

    @Id
    private String id; // ✅ MongoDB에서 자동 생성 가능

    private Long jobId;
    private Long userId;
    private String inputLanguage;
    private String s3Path;
    private Date savedAt;

    // Getters & Setters
}
