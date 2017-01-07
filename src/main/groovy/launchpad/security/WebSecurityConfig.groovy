package launchpad.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * General Spring Web Security configuration that defines a custom UserDetailsService for looking up and authenticating
 * users of the app. Also configures security rules and methods of authenticating.
 *
 * NOTE: The OAuth2Config.ResourceServerConfig has a higher order than this WebSecurityConfig. The rules defined in
 * this class will be executed AFTER the ResourceServerConfig rules.
 */
@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private PermissionBasedUserDetailsService permissionBasedUserDetailsService

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder()
    }

    @Autowired
    void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth

            // Register the custom Permission Based User Details Service
            .userDetailsService(permissionBasedUserDetailsService)

            // Override the default password encoder
            .passwordEncoder(passwordEncoder())
    }

    /**
     * HTTP authorizations are applied to each request in the order that they appear below. The first matcher to match
     * the request will be applied.
     *
     * NOTE: The OAuth2Config.ResourceServerConfig has a higher order than this WebSecurityConfig. The rules defined in
     * this class will be executed AFTER the ResourceServerConfig rules.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http

            // Enforce every request to be authenticated
            .authorizeRequests()
                .anyRequest().authenticated()
                .and()

            // Enable Form Login for users.
            //
            // DEVELOPER NOTE
            // For whatever reason, the .formLogin() definitions need to be in both the WebSecurityConfig and
            // the ResourceServerConfig (this config). If only WebSecurityConfig has the .formLogin() definition,
            // then the ResourceServerConfig wont inject the OAuth2AuthenticationProcessingFilter into the servlet
            // filter chain for handling incoming Authorization bear tokens. If only ResourceServerConfig has the
            // .formLogin() definition, then WebSecurityConfig wont inject the UsernamePasswordAuthenticationFilter
            // or the BasicAuthenticationFilter. Including .formLogin() in both config files ensures all 3 filters
            // are included. Based on StackOverflow chatter, this is a known bug for SpringBoot 1.4 w/ Spring Security
            // + OAuth2. -- Tim Michalski 1/6/2017
            .formLogin()
                .loginPage('/login').permitAll()
                .and()

            // Enable Basic Auth login for users and clients. This is primarily for client login directly to the
            // /oauth/token service.
            .httpBasic().and()

            // Disable CSRF due to this app being an API using JWT bearer token and not session based for resources.
            .csrf().disable()
    }

    @Override
    void configure(WebSecurity web) throws Exception {
        web
            // Ignore authentication requirements on the following URL endpoints
            .ignoring()
            .antMatchers('/hello')
    }
}