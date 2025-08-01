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
public class AdminUserReadModelViewHandler {

    //<<< DDD / CQRS
    @Autowired
    private AdminUserReadModelRepository adminUserReadModelRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenUserRegistered_then_CREATE_1(
        @Payload UserRegistered userRegistered
    ) {
        try {
            if (!userRegistered.validate()) return;

            // view 객체 생성
            AdminUserReadModel adminUserReadModel = new AdminUserReadModel();
            // view 객체에 이벤트의 Value 를 set 함
            adminUserReadModel.setId(userRegistered.getId());
            adminUserReadModel.setEmail(userRegistered.getEmail());
            adminUserReadModel.setRole(userRegistered.getRole());
            adminUserReadModel.setNickname(userRegistered.getNickname());
            adminUserReadModel.setTokenIssued(userRegistered.getTokenIssued());
            // view 레파지 토리에 save
            adminUserReadModelRepository.save(adminUserReadModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenTokenRequested_then_UPDATE_1(
        @Payload TokenRequested tokenRequested
    ) {
        try {
            if (!tokenRequested.validate()) return;
            // view 객체 조회
            Optional<AdminUserReadModel> adminUserReadModelOptional = adminUserReadModelRepository.findById(
                tokenRequested.getId()
            );

            if (adminUserReadModelOptional.isPresent()) {
                AdminUserReadModel adminUserReadModel = adminUserReadModelOptional.get();
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                adminUserReadModel.setTokenIssued(tokenRequested.isTokenIssued());
                // view 레파지 토리에 save
                adminUserReadModelRepository.save(adminUserReadModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //>>> DDD / CQRS
}
