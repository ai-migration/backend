package backend.domain;

import backend.AdminApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Token_table")
@Data
//<<< DDD / Aggregate Root
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long userId; // user 도메인에서의 id, 유사 외래키임

    private String apiKey;

    private Boolean active;

    private Date createdAt;

    public static TokenRepository repository() {
        TokenRepository tokenRepository = AdminApplication.applicationContext.getBean(
            TokenRepository.class
        );
        return tokenRepository;
    }

    

    //<<< Clean Arch / Port Method
    public static void requestTokenPolicy(TokenRequested tokenRequested) {
        //implement business logic here:

        // api key 하드코딩
        final String STATIC_API_KEY = "your-hardcoded-api-key";

        // Repository 직접 접근 (기존 구조 유지)
        TokenRepository tokenRepository = AdminApplication.applicationContext.getBean(TokenRepository.class);

        // 이미 발급된 토큰인지 확인 (중복 방지)
        boolean alreadyExists = tokenRepository.existsByUserId(tokenRequested.getId());
        if (alreadyExists || tokenRequested.isTokenIssued()) {
            System.out.println("이미 토큰이 발급된 사용자입니다. userId=" + tokenRequested.getId());
            return;
    }


        // Example 1:  new item 
        Token token = new Token();
        token.setUserId(tokenRequested.getId());
        token.setApiKey(STATIC_API_KEY);
        token.setActive(true);
        token.setCreatedAt(new Date());

        repository().save(token);

        /** Example 2:  finding and process
        

        repository().findById(tokenRequested.get???()).ifPresent(token->{
            
            token // do something
            repository().save(token);


         });
        */

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
