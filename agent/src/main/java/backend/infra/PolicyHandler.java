package backend.infra;

import backend.config.kafka.KafkaProcessor;
import backend.dto.AgentEvent;
import backend.dto.ConversionResponse;
import backend.dto.Report;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.transaction.Transactional;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
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

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(Message<String> message) throws JsonProcessingException {
        Object v = message.getHeaders().get("AGENT"); // 보통 byte[]
        String agent = null;
        if (v instanceof byte[]) {
            agent = new String((byte[]) v, StandardCharsets.UTF_8);
        } else if (v != null) {
            agent = v.toString(); // 혹시 String으로 매핑된 경우
        }

        String payload = message.getPayload(); // 실제 메세지
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println(message);
        System.out.println(payload);
        System.out.println(agent);
        if ("EGOV".equals(agent != null ? agent.trim() : null)) {
            String normalized = payload.trim();

            AgentEvent agentEvent = mapper.readValue(normalized, AgentEvent.class);

            sseController.sendMessage(agentEvent);

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

//        } catch (Exception ex) {
//            System.out.println(ex);
//        }
    }
}
//>>> Clean Arch / Inbound Adaptor
