package backend.infra;

import backend.dto.AgentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
public class SseController {

    // (userId, jobId) -> SseEmitter 매핑
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping(value = "/response/{userId}/{jobId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable Long userId, @PathVariable Long jobId) {
        String key = getKey(userId, jobId);

        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(key, emitter);

        emitter.onCompletion(() -> emitters.remove(key));
        emitter.onTimeout(() -> emitters.remove(key));
        emitter.onError(e -> emitters.remove(key));

        return emitter;
    }

    public void sendMessage(AgentEvent event) {
        String key = getKey(event.getUserId(), event.getJobId());
        SseEmitter emitter = emitters.get(key);
        if (emitter == null) return;

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("agent-message")                // 프론트에서 이 이름으로 구독
                            .data(event)                         // Jackson이 JSON으로 직렬화
            );
        } catch (IOException e) {
            emitters.remove(key);
        }
    }

    private String getKey(Long userId, Long jobId) {
        return userId + ":" + jobId;
    }
}