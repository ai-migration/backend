package backend.infra;

import backend.domain.*;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/agents")
@Transactional
public class AgentController {

    @Autowired
    AgentRepository agentRepository;
    @Autowired
    private S3Client s3Client;
    @Autowired
    private S3Presigner presigner;
    @Autowired
    private ConversionLogRepository conversionLogRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @PostMapping(value = "/conversion", consumes = "multipart/form-data")
    public ResponseEntity<?> conversion(@RequestPart("agent") Agent agent, @RequestPart("file") MultipartFile file) throws IOException {
        System.out.println("변환 요청");

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 없습니다.");
        }

        String fileName = file.getOriginalFilename();
        String s3SavePath = agent.getUserId() + "/" + agent.getId() + "/" + fileName;
        System.out.println("s3SavePath:" + s3SavePath);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3SavePath)
                .contentType("application/zip")
                .build();

        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

        ConversionLog log = new ConversionLog();
        log.setJobId(agent.getId());
        log.setUserId(agent.getUserId());
        log.setS3Path(s3SavePath);
        log.setSavedAt(new Date());
        ConversionLog saved = conversionLogRepository.save(log);
        System.out.println("Saved document: " + saved);

        // agent 기능 구현 완료 시 주석 해제
//        agent.conversionEvent();
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/security")
    public ResponseEntity<?> security(@RequestBody Agent agent) {
        System.out.println("보안 분석 요청");
        agent.securityEvent();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/chatbot")
    public ResponseEntity<?> chatbot(@RequestBody Agent agent) {
        System.out.println("챗봇 요청");
        agent.chatbotEvent();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download/{userId}/{jobId}")
    public ResponseEntity<?> download(@PathVariable Long userId, @PathVariable Long jobId) {
        System.out.println("다운로드 요청");
        ConversionLog log = conversionLogRepository.findByJobIdAndUserId(jobId, userId)
                .orElseThrow(() -> new RuntimeException("해당 파일을 찾을 수 없습니다."));

        String s3Path = log.getS3Path();

        // Presigned URL 생성
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Path)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();
        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        String presignedUrl = presignedRequest.url().toString();

        Map<String, String> response = new HashMap<>();
        response.put("path", presignedUrl);

        return ResponseEntity.ok(response);
    }
}
//>>> Clean Arch / Inbound Adaptor
