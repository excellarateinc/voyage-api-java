package voyage.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class PasswordEncoderConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder()
        Map<String, PasswordEncoder> encoderMap = ['bcrypt':bcrypt]
        DelegatingPasswordEncoder delegating = new DelegatingPasswordEncoder('bcrypt', encoderMap)
        delegating.setDefaultPasswordEncoderForMatches(bcrypt)
        return delegating
    }
}
