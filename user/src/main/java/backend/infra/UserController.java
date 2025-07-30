package backend.infra;

import backend.domain.*;
import backend.dto.LoginRequest;
import backend.dto.MypageResponse;
import backend.dto.RegisterRequest;
import backend.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/users")
@RequiredArgsConstructor
@Transactional
public class UserController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // 1. 회원가입
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        //TODO: process POST request
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());

        User saved = userRepository.save(user);
        new UserRegistered(saved).publish();
        return ResponseEntity.ok("회원가입 완료");
    }

    // 2. 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        //TODO: process POST request
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if(userOpt.isPresent() && userOpt.get().getPassword().equals(request.getPassword())){
            User user = userOpt.get();
            new UserLoggedIn(user).publish();

            String token = jwtUtil.generateToken(user.getId());
            return ResponseEntity.ok("Bearer " + token);

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
        }
    }

    // 3. 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String accessToken) {
        //TODO: process POST request
        Long userId = jwtUtil.extractUserIdFromToken(accessToken);
        userRepository.findById(userId).ifPresent(user -> new UserLoggedOut(user).publish());
        return ResponseEntity.ok("로그아웃 완료");
    }
    
    // 4. 토큰 발급 요청
    @PostMapping("/token")
    public ResponseEntity<?> requestToken(@RequestHeader("Authorization") String accessToken) {
        //TODO: process POST request
        Long userId = jwtUtil.extractUserIdFromToken(accessToken);
        User user = userRepository.findById(userId).orElseThrow();
        user.setTokenIssued(true);
        userRepository.save(user);
        new TokenRequested(user).publish();
        return ResponseEntity.ok("토큰 발급 요청됨");
    }
    

    // 5. 마이페이지 조회
    @GetMapping("/mypage")
    public ResponseEntity<?> mypage(@RequestHeader("Authorization") String accessToken) {
        Long userId = jwtUtil.extractUserIdFromToken(accessToken); // 사용자 ID 추출
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.")); // 유저 조회
        
        MypageResponse response = new MypageResponse(
            user.getEmail(),
            user.getNickname(),
            user.getTokenIssued());
        return ResponseEntity.ok(response);

    }
    
    
}
//>>> Clean Arch / Inbound Adaptor
