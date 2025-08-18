package backend.infra;

import backend.domain.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UriUtils;
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

    private String generatePresignedUrl(String bucketName, String s3Path, Duration expireDuration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Path)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expireDuration)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    @PostMapping(value = "/conversion", consumes = "multipart/form-data")
    public ResponseEntity<?> conversion(@RequestPart("agent") Agent agent, @RequestPart("file") MultipartFile file) throws IOException {
        System.out.println("변환 요청");

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 없습니다.");
        }

        String fileName = file.getOriginalFilename();
        String s3SavePath = agent.getUserId() + "/" + agent.getJobId() + "/" + fileName;
        System.out.println("s3SavePath:" + s3SavePath);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3SavePath)
                .contentType("application/zip")
                .build();

        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

        String presignedUrl = generatePresignedUrl(bucketName, s3SavePath, Duration.ofMinutes(60));

        ConversionLog log = new ConversionLog();
        log.setJobId(agent.getJobId());
        log.setUserId(agent.getUserId());
        log.setS3OriginPath(s3SavePath);
        log.setSavedAt(new Date());
        ConversionLog saved = conversionLogRepository.save(log);
        System.out.println("Saved document: " + saved);

        // agent 기능 구현 완료 시 주석 해제
        agent.setFilePath(presignedUrl);
        agent.conversionEvent();
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

    @GetMapping("/download/**")
    public ResponseEntity<?> download(HttpServletRequest request) {
        System.out.println("다운로드 요청");

        // 매칭된 패턴과 실제 경로 추출
        String pathWithinHandler = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE); // /agents/download/0/3/conversion/controller/EgovBoardController.java
        String bestMatchPattern = (String) request.getAttribute(
                HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE); // /agents/download/**

        // "download/" 이후의 경로만 추출됨
        String extractedPath = new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, pathWithinHandler);

        // URL 디코딩 (한글 파일명/폴더 방지)
        String decodedPath = UriUtils.decode(extractedPath, StandardCharsets.UTF_8);

        String presignedUrl = generatePresignedUrl(bucketName, decodedPath, Duration.ofMinutes(10));

        Map<String, String> response = new HashMap<>();
        response.put("path", presignedUrl);

        return ResponseEntity.ok(response);
    }


    // 변환 이력 조회
    @GetMapping("/records/{userId}")
    public ResponseEntity<?> getRecords(@PathVariable Long userId) {

        List<ConversionLog> logs = conversionLogRepository.findAllByUserIdOrderBySavedAtDesc(userId);
        // 비어있으면 빈 배열로 200 반환
        return ResponseEntity.ok(logs);
    }

    // 변환 이력 상세 조회
    @GetMapping("/records/detail/{jobId}")
    public ResponseEntity<?> getRecordDetail(@PathVariable Long jobId) {
        ConversionLog log = conversionLogRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("해당 파일을 찾을 수 없습니다."));
        // 비어있으면 빈 배열로 200 반환
        return ResponseEntity.ok(log);
    }

}
//>>> Clean Arch / Inbound Adaptor
