package backend.infra;

import backend.config.kafka.KafkaProcessor;
import backend.domain.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.naming.NameParser;
import javax.naming.NameParser;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

//<<< Clean Arch / Inbound Adaptor
@Service
@Transactional
public class PolicyHandler {

    @Autowired
    PostRepository postRepository;

    @Autowired
    PostQueryRepository postQueryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PostRegisterRequested'"
    )
    public void handlePostRegisterRequested(@Payload byte[] rawBytes) {
        try {
        // Step 1. byte[] → String
            String rawJson = new String(rawBytes, "UTF-8");
            System.out.println("🟢 Kafka JSON 수신: " + rawJson);

        // Step 2. JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> wrapper = mapper.readValue(rawJson, Map.class);
            Map<String, Object> payload = (Map<String, Object>) wrapper.get("payload");

        // Step 3. DTO로 변환
            PostRegisterRequested event = mapper.convertValue(payload, PostRegisterRequested.class);
            System.out.println("✅ 이벤트 매핑 성공: " + event);

        // Step 4. 저장
            Post post = new Post();
            post.setPostId(event.getPostId());
            post.setTitle(event.getTitle());
            post.setContent(event.getContent());
            post.setType(event.getType());

            postRepository.save(post);

            System.out.println("✅ Post 저장 완료");

        } catch (Exception e) {
            System.out.println("❌ 메시지 처리 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PostDeleteRequested'"
    )
    public void wheneverPostDeleteRequested_DeletePostPolicy(
        @Payload PostDeleteRequested postDeleteRequested
    ) {
        PostDeleteRequested event = postDeleteRequested;
        System.out.println(
            "\n\n##### listener DeletePostPolicy : " +
            postDeleteRequested +
            "\n\n"
        );

        // Sample Logic //
        Post post = postRepository.findByPostId(event.getPostId())
            .orElseThrow(() -> new RuntimeException("Post Not Found"));
        postRepository.delete(post);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PostUpdateRequested'"
    )
    public void wheneverPostUpdateRequested_UpdatePostPolicy(
        @Payload PostUpdateRequested postUpdateRequested
    ) {
        PostUpdateRequested event = postUpdateRequested;
        System.out.println(
            "\n\n##### listener UpdatePostPolicy : " +
            postUpdateRequested +
            "\n\n"
        );
        Post post = postRepository.findByPostId(event.getPostId()).orElseThrow(() -> new RuntimeException("Post Not Found"));

        // Sample Logic //
        post.setTitle(event.getTitle());
        post.setContent(event.getContent());
        post.setType(event.getType());

        postRepository.save(post);
    }
}
//>>> Clean Arch / Inbound Adaptor
