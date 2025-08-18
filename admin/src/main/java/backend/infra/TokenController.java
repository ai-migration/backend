package backend.infra;

import backend.domain.*;
import backend.dto.AdminUserDetailResponseDto;
import backend.dto.AdminUserListResponseDto;
import backend.dto.UpdateUserRoleRequestDto;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.Date;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/admin")
@RequiredArgsConstructor
@Transactional
public class TokenController {

    private final TokenRepository tokenRepository;
    private final AdminUserReadModelRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 1. openAPI 키 발급 요청 처리 (policy)
    // policyhandlder 에서 처리


    // 2. 전체 회원 목록 조회 (command)
    @GetMapping("/users")
    public List<AdminUserListResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
            .filter(user -> user.getRole() == RoleType.USER)
            .map(user -> {
                AdminUserListResponseDto dto = new AdminUserListResponseDto();
                dto.setUserId(user.getId());
                dto.setEmail(user.getEmail());
                dto.setNickname(user.getNickname());
                dto.setRole(user.getRole());
                dto.setTokenIssued(user.isTokenIssued());
                return dto;
            })
            .collect(Collectors.toList());
    }
    

    // 3. 특정 회원 상세 정보 조회 (command)
    @GetMapping("/users/{userId}")
    public AdminUserDetailResponseDto getUserDetail(@PathVariable Long userId) {
        // AdminUserReadModel의 id가 userId랑 일치하는지 확인
        AdminUserReadModel user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자 없음"));
        // Token 엔티티의 userId 로 나머지값을 가져온다
        Optional<Token> tokenOpt = tokenRepository.findByUserId(userId);
        AdminUserDetailResponseDto dto = new AdminUserDetailResponseDto();
        dto.setUserId(user.getId());
        dto.setEmail(user.getEmail());
        // dto.setPassword(user.getPassword()); 보안상 생략
        dto.setNickname(user.getNickname());
        dto.setRole(user.getRole());
        dto.setTokenIssued(user.isTokenIssued());

        tokenOpt.ifPresent(token -> {
            dto.setApiKey(token.getApiKey());
            dto.setActive(token.getActive());
            dto.setCreatedAt(token.getCreatedAt());
        });

        return dto;

    }

    // 4. 사용자 권한 변경
    @PatchMapping("users/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestBody UpdateUserRoleRequestDto request){
        // readmodel 수정
        AdminUserReadModel user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자 없음"));
        user.setRole(request.getRole());
        userRepository.save(user);

        // kafka 이벤트 발행
        UserRoleUpdated event = new UserRoleUpdated();
        event.setUserId(user.getId());
        event.setRole(request.getRole());
        event.publishAfterCommit();
        
        return ResponseEntity.ok("사용자 권한이 " + request.getRole() + "로 변경되었습니다.");

    }
    

    // 5. 게시물 등록 (command) => 게시판도메인에서 처리
    @PostMapping("/posts")
    public String registerPost(@RequestBody PostRegisterRequested event) {
        //TODO: process POST request
        Message<PostRegisterRequested> message = MessageBuilder
            .withPayload(event)
            .setHeader("type", "PostRegisterRequested")
            .build();
        event.publishAfterCommit();
        System.out.println("✅ Kafka 발행 성공!");
        return "게시글 등록 요청됨";
    }
    
    
    // 6. 게시물 수정 (comman d) => 게시판도메인에서 처리
    @PutMapping("/posts/{postId}")
    public String updatePost(@RequestBody PostUpdateRequested event) {
        //TODO: process POST request
        Message<PostUpdateRequested> message = MessageBuilder
            .withPayload(event)
            .setHeader("type", "PostUpdateRequested")
            .build();
        event.publishAfterCommit();
        return "게시글 수정 요청됨";
    }
    

    // 7. 게시물 삭제 (command) => 게시판도메인에서 처리
    @DeleteMapping("/posts/{postId}")
    public String deletePost(@RequestBody PostDeleteRequested event) {
        //TODO: process POST request
        Message<PostDeleteRequested> message = MessageBuilder
            .withPayload(event)
            .setHeader("type", "PostDeleteRequested")
            .build();
        event.publishAfterCommit();
        return "게시글 삭제 요청됨";
    }

    ///////////////////////////
    /// test progress
    /// ////////////////////////////
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @GetMapping(value = "/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam("userId") Long userId, @RequestParam("jobId") String jobId) {
        SseEmitter emitter = new SseEmitter(0L); // 타임아웃 없음
        System.out.println("userId: " + userId);
        System.out.println("jobId: " + jobId);

        executor.submit(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    // 1~3초 랜덤 대기
                    long delay = ThreadLocalRandom.current().nextLong(1000, 3001);
                    Thread.sleep(delay);

                    String msg = "메시지 " + i + " (지연: " + (delay / 1000) + "초)";
                    emitter.send(SseEmitter.event()
                            .name("step")
                            .id(String.valueOf(i))
                            .data(msg));

                    System.out.println("전송: " + msg);
                }

                emitter.send(SseEmitter.event().name("done").data("모든 메시지 전송 완료"));
                emitter.complete();
            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

}
//>>> Clean Arch / Inbound Adaptor
