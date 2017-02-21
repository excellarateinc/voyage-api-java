package voyage.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

@Configuration
@EnableJpaAuditing
class AuditConfig {

    @Bean
    AuditorAware<String> getAuditorProvider() {
        return new SpringSecurityAuditorAware()
    }

    class SpringSecurityAuditorAware implements AuditorAware<String> {
        String getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.context.authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return null
            }
            return authentication.principal
        }
    }
}
