package backend.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // {bcrypt} 를 기본으로 하는 DelegatingPasswordEncoder 반환
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        // 만약 강도를 지정한 순수 BCrypt만 쓸거면:
        // return new BCryptPasswordEncoder(12);
    }
}