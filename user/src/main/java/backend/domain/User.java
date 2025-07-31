package backend.domain;

import backend.UserApplication;
import backend.domain.TokenRequested;
import backend.domain.UserLoggedIn;
import backend.domain.UserLoggedOut;
import backend.domain.UserRegistered;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import lombok.Data;

@Entity
@Table(name = "User_table")
@Data
//<<< DDD / Aggregate Root
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")    
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")   
    private String password;

    private String nickname;

    @Column(nullable = false)
    private String role = "USER";

    @Column(nullable = false)
    private Boolean tokenIssued = false;

    @PostPersist
    public void onPostPersist() {
        UserLoggedIn userLoggedIn = new UserLoggedIn(this);
        userLoggedIn.publishAfterCommit();

        UserLoggedOut userLoggedOut = new UserLoggedOut(this);
        userLoggedOut.publishAfterCommit();

        UserRegistered userRegistered = new UserRegistered(this);
        userRegistered.publishAfterCommit();

        TokenRequested tokenRequested = new TokenRequested(this);
        tokenRequested.publishAfterCommit();
    }

    public static UserRepository repository() {
        UserRepository userRepository = UserApplication.applicationContext.getBean(
            UserRepository.class
        );
        return userRepository;
    }
}
//>>> DDD / Aggregate Root
