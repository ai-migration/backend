package backend.infra;

import backend.domain.*;

import java.io.IOException;
import java.util.UUID;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/agents")
@Transactional
public class AgentController {

    @Autowired
    AgentRepository agentRepository;
    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @PostMapping(value = "/conversion", consumes = "multipart/form-data")
    public ResponseEntity<?> conversion(@RequestPart("agent") Agent agent, @RequestPart("file") MultipartFile file) throws IOException {
        System.out.println("변환 요청");

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 없습니다.");
        }

        String fileName = file.getOriginalFilename();
        String s3SavePath = agent.getUserId() + "/" + UUID.randomUUID() + "-" + fileName;
        System.out.println("s3SavePath:" + s3SavePath);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3SavePath)
                .contentType("application/zip")
                .build();

        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));
        String fileUrl = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/" + s3SavePath;
        System.out.println("S3저장완료: " + fileUrl);
        
        // TO-BE: db에 s3SavePath 저장
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
}
//>>> Clean Arch / Inbound Adaptor
