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

    private Long userId; // user 도메인에서의 id, 외래키임

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

        // Example 1:  new item 
        Token token = new Token();
        token.setUserId(tokenRequested.getUserId());
        token.setApiKey(UUID.randomUUID().toString());
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
