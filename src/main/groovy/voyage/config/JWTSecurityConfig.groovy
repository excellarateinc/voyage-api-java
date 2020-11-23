package voyage.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
import org.springframework.security.web.authentication.HttpStatusEntryPoint

@Configuration
@Order(-1000)
class JWTSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value('${security.oauth2.resourceserver.opaque.introspection-uri}')
    protected String introspectionUri

    @Value('${security.oauth2.resourceserver.opaque.introspection-client-id}')
    protected String clientId

    @Value('${security.oauth2.resourceserver.opaque.introspection-client-secret}')
    protected String clientSecret

    @Value('${security.permitAll}')
    private String[] permitAllUrls

    @Bean
    OpaqueTokenIntrospector introspector() {
        new CustomAuthoritiesOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret)
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
               // TODO Fix .cors().and()

                .exceptionHandling()
                .accessDeniedPage(null)
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()

                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                .csrf().disable()

                .oauth2ResourceServer()
                .opaqueToken( { opaqueTokenConfigurer  ->
                    opaqueTokenConfigurer.introspectionUri(this.introspectionUri)
                            .introspectionClientCredentials(this.clientId, this.clientSecret)
                    opaqueTokenConfigurer.introspector(introspector())
                })

        http.authorizeRequests()
                .antMatchers(permitAllUrls).permitAll()
                .anyRequest().authenticated()
    }

}