package backend.infra;

import backend.config.kafka.KafkaProcessor;
import backend.domain.*;
import backend.dto.AgentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

//<<< Clean Arch / Inbound Adaptor
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PolicyHandler {

    private final AgentRepository agentRepository;
    private final AgentEventStore store;     // 인메모리/Redis 등
    private final ObjectMapper om;           // 스프링 빈으로 주입
    private final SseController sseController;

    private final ObjectMapper mapper = new ObjectMapper();

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {
        try {
            System.out.println(eventString);
            String normalized = eventString.trim();
            AgentEvent agentEvent = mapper.readValue(normalized, AgentEvent.class);
            sseController.sendMessage(agentEvent);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
//>>> Clean Arch / Inbound Adaptor
