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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

@Entity
@Table(name = "User_table")
@Data
//<<< DDD / Aggregate Root
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()_+=]).*$",
         message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank
    private String nickname;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @Column(nullable = false)
    private Boolean tokenIssued = false;

//    @PostPersist
//    public void onPostPersist() {
//        UserLoggedIn userLoggedIn = new UserLoggedIn(this);
//        userLoggedIn.publishAfterCommit();
//
//        UserLoggedOut userLoggedOut = new UserLoggedOut(this);
//        userLoggedOut.publishAfterCommit();
//
//        UserRegistered userRegistered = new UserRegistered(this);
//        userRegistered.publishAfterCommit();
//
//        TokenRequested tokenRequested = new TokenRequested(this);
//        tokenRequested.publishAfterCommit();
//    }

    public static UserRepository repository() {
        UserRepository userRepository = UserApplication.applicationContext.getBean(
            UserRepository.class
        );
        return userRepository;
    }
}
//>>> DDD / Aggregate Root
