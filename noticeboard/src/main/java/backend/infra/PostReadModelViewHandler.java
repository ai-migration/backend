package backend.infra;

import backend.config.kafka.KafkaProcessor;
import backend.domain.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PostReadModelViewHandler {

    //<<< DDD / CQRS
    @Autowired
    private PostReadModelRepository postReadModelRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPostRegistered_then_CREATE_1(
        @Payload PostRegistered postRegistered
    ) {
        try {
            if (!postRegistered.validate()) return;

            // view 객체 생성
            PostReadModel postReadModel = new PostReadModel();
            // view 객체에 이벤트의 Value 를 set 함
            postReadModel.setPostId(postRegistered.getPostId());
            postReadModel.setTitle(postRegistered.getTitle());
            postReadModel.setContent(postRegistered.getContent());
            postReadModel.setType(postRegistered.getType());
            postReadModel.setCreatedAt(postRegistered.getCreatedAt());
            postReadModel.setViewCount(0);
            // view 레파지 토리에 save
            postReadModelRepository.save(postReadModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPostUpdated_then_UPDATE_1(
        @Payload PostUpdated postUpdated
    ) {
        try {
            if (!postUpdated.validate()) return;
            // view 객체 조회
            Optional<PostReadModel> postReadModelOptional = postReadModelRepository.findByPostId(
                postUpdated.getPostId()
            );

            if (postReadModelOptional.isPresent()) {
                PostReadModel postReadModel = postReadModelOptional.get();
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                postReadModel.setTitle(postUpdated.getTitle());
                postReadModel.setContent(postUpdated.getContent());
                postReadModel.setUpdatedAt(postUpdated.getUpdatedAt());
                // view 레파지 토리에 save
                postReadModelRepository.save(postReadModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //>>> DDD / CQRS

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPostDeleted_then_DELETE_1(
        @Payload PostDeleted postDeleted
    ) {
        try {
            if (!postDeleted.validate()) return;
            // view 레파지 토리에 삭제 쿼리
            postReadModelRepository.deleteById(postDeleted.getPostId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //>>> DDD / CQRS
}
