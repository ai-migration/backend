package backend.infra;

import backend.config.kafka.KafkaProcessor;
import backend.dto.AgentEvent;
import backend.dto.ConversionResponse;
import backend.dto.Report;
import backend.dto.SecurityAgentEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import javax.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.nio.charset.StandardCharsets;
import java.util.*;

//<<< Clean Arch / Inbound Adaptor
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PolicyHandler {

    private final SseController sseController;

    private final ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private ConversionLogRepository conversionLogRepository;

    @Autowired
    private SecurityLogRepository securityLogRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(Message<String> message) throws Exception {
        Object v = message.getHeaders().get("AGENT"); // 보통 byte[]
        String agent = null;
        if (v instanceof byte[]) {
            agent = new String((byte[]) v, StandardCharsets.UTF_8);
        } else if (v != null) {
            agent = v.toString(); // 혹시 String으로 매핑된 경우
        }

        String payload = message.getPayload(); // 실제 메세지

        System.out.println("[agent] " + agent + "[message] " + payload);

        String normalized = payload.trim();

        AgentEvent agentEvent = mapper.readValue(normalized, AgentEvent.class);

        sseController.sendMessage(agentEvent);
        
        if ("EGOV".equals(agent != null ? agent.trim() : null)) {

            // 결과 저장
            if (agentEvent.getResult() != null) {
                ConversionResponse convResult = agentEvent.getResult();

                List<String> controller = convResult.getControllerEgov();
                List<String> service = convResult.getServiceEgov();
                List<String> serviceImpl = convResult.getServiceimplEgov();
                List<String> vo = convResult.getVoEgov();

                ConversionLog log = conversionLogRepository.findByUserIdAndJobId(agentEvent.getUserId(), agentEvent.getJobId())
                        .orElseThrow(() -> new RuntimeException("해당 파일을 찾을 수 없습니다."));

                if (controller != null && !controller.isEmpty()) {
                    var savedController = SavedResult.uploadAndBuildReport(
                            controller, convResult.getControllerReport(), "controller", bucketName, s3Client, agentEvent.getUserId(), agentEvent.getJobId()
                    );
                    log.setConvControllerReport(savedController.getReportList());
                    log.setS3ConvControllerPath(savedController.getPathList());
                }

                if (service != null && !service.isEmpty()) {
                    var savedService = SavedResult.uploadAndBuildReport(
                            service, convResult.getServiceReport(), "service", bucketName, s3Client, agentEvent.getUserId(), agentEvent.getJobId()
                    );
                    log.setConvServiceReport(savedService.getReportList());
                    log.setS3ConvServicePath(savedService.getPathList());
                }

                if (serviceImpl != null && !serviceImpl.isEmpty()) {
                    var savedServiceImpl = SavedResult.uploadAndBuildReport(
                            serviceImpl, convResult.getServiceimplReport(), "serviceImpl", bucketName, s3Client, agentEvent.getUserId(), agentEvent.getJobId()
                    );
                    log.setConvServiceimplReport(savedServiceImpl.getReportList());
                    log.setS3ConvServiceimplPath(savedServiceImpl.getPathList());
                }

                if (vo != null && !vo.isEmpty()) {
                    var savedVoRes = SavedResult.uploadAndBuildReport(
                            vo, convResult.getVoReport(), "vo", bucketName, s3Client, agentEvent.getUserId(), agentEvent.getJobId()
                    );
                    log.setConvVoReport(savedVoRes.getReportList());
                    log.setS3ConvVoPath(savedVoRes.getPathList());
                }

                ConversionLog saved = conversionLogRepository.save(log);
            }
        }
        if ("SECU".equals(agent != null ? agent.trim() : null)) {
            // 1) 로컬 매퍼: 기본 ObjectMapper
            ObjectMapper json  = new ObjectMapper();   // 기본 매퍼: 필드명이 JSON과 동일하면 OK
            // 2) DTO로 안전 파싱
            SecurityAgentEvent ev = json.readValue(payload.trim(), SecurityAgentEvent.class);

            Long userId = ev.getUserId();
            Long jobId  = ev.getJobId();

            SecurityLog log = securityLogRepository.findByUserIdAndJobId(userId, jobId)
                .orElseThrow(() -> new RuntimeException("보안 로그 문서를 찾을 수 없습니다."));
            
            // 3) result가 있을 때만 S3 업로드 수행
            if (ev.getResult() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resultMap = json.convertValue(ev.getResult(), Map.class);

                SecuritySavedResult.ProcessResult pr =
                    SecuritySavedResult.uploadAll(resultMap, bucketName, s3Client, userId, jobId);

                log.setS3AgentInputsPath(pr.getS3AgentInputsPath());
                log.setS3ReportsDir(pr.getS3ReportsDir());
                log.setS3ReportJsonPath(pr.getS3ReportJsonPath());
                log.setIssueReportFiles(pr.getIssueReportFiles());
                log.setIssueCount(pr.getIssueCount());
            }

            // (선택) 종료 시각 갱신
            log.setSavedAt(new Date());

            securityLogRepository.save(log);
        }

//        } catch (Exception ex) {
//            System.out.println(ex);
//        }
    }
}
//>>> Clean Arch / Inbound Adaptor
