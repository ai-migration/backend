package backend.infra;

import backend.domain.*;
import backend.dto.AdminUserDetailResponseDto;
import backend.dto.AdminUserListResponseDto;
import lombok.RequiredArgsConstructor;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;


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
            dto.setCreatedAt(token.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        });

        return dto;

    }
    

    // 4. 게시물 등록 (command) => 게시판도메인에서 처리
    @PostMapping("/posts")
    public String registerPost(@RequestBody PostRegisterRequested event) {
        //TODO: process POST request
        kafkaTemplate.send("PostRegisterRequested", event);  // Kafka 발행
        return "게시글 등록 요청됨";
    }
    
    
    // 5. 게시물 수정 (command) => 게시판도메인에서 처리
    @PutMapping("/posts/{postId}")
    public String updatePost(@RequestBody PostUpdateRequested event) {
        //TODO: process POST request
        kafkaTemplate.send("PostUpdateRequested", event);
        return "게시글 수정 요청됨";
    }
    

    // 6. 게시물 삭제 (command) => 게시판도메인에서 처리
    @DeleteMapping("/posts/{postId}")
    public String deletePost(@RequestBody PostDeleteRequested event) {
        //TODO: process POST request
        kafkaTemplate.send("PostDeleteRequested", event);
        return "게시글 삭제 요청됨";
    }
    

}
//>>> Clean Arch / Inbound Adaptor
