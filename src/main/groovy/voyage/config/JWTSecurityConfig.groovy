package voyage.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.authentication.HttpStatusEntryPoint

@Configuration
@Order(-1000)
class JWTSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String API_PATH = '/api/**'

    @Value('${security.oauth2.resourceserver.jwt.jwk-set-uri}')
    private String jwkSetUri

    @Value('${security.permitAll}')
    private String[] permitAllUrls

    @Override
    protected void configure(HttpSecurity http) {
        http
                .sessionManagement()
        // Do not maintain session state between requests or support cookies.
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                .authorizeRequests()

        // Allow any request to access things like 'status' or 'css'
                .antMatchers(permitAllUrls).permitAll()

        // Enforce every request to be authenticated
                .anyRequest().authenticated()
                .and()

                .exceptionHandling()
                .accessDeniedPage(null)
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()

        // Disable CSRF due to this app being an API using JWT bearer token and not session based for resources.
                .csrf().disable()

                .oauth2ResourceServer({ oauth2 -> oauth2.jwt() })
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build()
    }
}
