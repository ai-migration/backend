package backend.infra;

import backend.domain.*;
import backend.dto.LoginRequestDto;
import backend.dto.LoginResponseDto;
import backend.dto.MypageResponseDto;
import backend.dto.RegisterRequestDto;
import backend.dto.RegisterResponseDto;
import backend.dto.PasswordChangeDto;
import backend.util.JwtUtil;
import backend.util.PasswordConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.Date;
import java.time.temporal.ChronoUnit;

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
    private final PasswordEncoder passwordEncoder;

    // 1. 회원가입
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto request) {
        //TODO: process POST request
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setRole(RoleType.USER);
        
        // (선택) 최초 변경일 기록: 유효기간 정책/알림용
        user.setPasswordChangedAt(new Date());

        User saved = userRepository.save(user);
        new UserRegistered(saved).publish();

        RegisterResponseDto response = new RegisterResponseDto(
            saved.getId(),
            saved.getEmail(),
            saved.getNickname(),
            saved.getRole()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        //TODO: process POST request
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());


        // email(사용자의 로그인Id) 존재 여부 확인
        if(userOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일이 존재하지 않습니다.");
        }
        
        User user = userOpt.get();

        // 비밀번호 암호화 비교
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 올바르지 않습니다.");
        }

        // 비밀번호 평문 비교
        // if (!user.getPassword().equals(request.getPassword())) {
        //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 올바르지 않습니다.");
        // }

        new UserLoggedIn(user).publish();

        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        String nicknameForResponse =
        (user.getRole() == RoleType.ADM)
                ? user.getNickname()
                : maskNickname(user.getNickname());

        Date date1 = user.getPasswordChangedAt();
        Date date2 = new Date();
        long diffInMillis = date2.getTime() - date1.getTime();
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

        boolean isExpired = diffInDays > 60 ? true : false;

        LoginResponseDto response = new LoginResponseDto("Bearer " + token, user.getId(), nicknameForResponse, user.getRole(), isExpired);
        return ResponseEntity.ok(response);
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
        
        
        String nicknameForResponse =
        (user.getRole() == RoleType.ADM)
                ? user.getNickname()
                : maskNickname(user.getNickname());

        MypageResponseDto response = new MypageResponseDto(
            user.getEmail(),
            nicknameForResponse,
            user.getTokenIssued());
        return ResponseEntity.ok(response);

    }
    

    // 6. 비밀번호 변경
    @PatchMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String accessToken, @RequestBody PasswordChangeDto request) {
        Long userId = jwtUtil.extractUserIdFromToken(accessToken); // 사용자 ID 추출
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.")); // 유저 조회

        // 비밀번호 암호화 비교
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 올바르지 않습니다.");
        }

        // 비밀번호 평문 비교
        // if (!user.getPassword().equals(request.getPassword())) {
        //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 올바르지 않습니다.");
        // }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(new Date());
        
        userRepository.save(user);

        String nicknameForResponse =
        (user.getRole() == RoleType.ADM)
                ? user.getNickname()
                : maskNickname(user.getNickname());

        MypageResponseDto response = new MypageResponseDto(
            user.getEmail(),
            nicknameForResponse,
            user.getTokenIssued());
        return ResponseEntity.ok(response);
    }
    

    // 닉네임 마스킹
    private String maskNickname(String nickname) {
        if (nickname == null || nickname.length() <= 2) {
            return "*".repeat(nickname.length()); // 1~2글자면 전부 마스킹
        }
        int visible = 1; // 앞뒤 1글자씩 보이게
        int maskLength = nickname.length() - visible * 2;
        return nickname.substring(0, visible) +
            "*".repeat(maskLength) +
            nickname.substring(nickname.length() - visible);
    }
}
//>>> Clean Arch / Inbound Adaptor
