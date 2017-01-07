package launchpad.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
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

            // Allow any user to access 'login' and web 'resources' like CSS/JS
            .authorizeRequests()
                .antMatchers('/resources/**', 'login').permitAll()
                .and()

            // Enforce every request to be authenticated
            .authorizeRequests()
                .anyRequest().authenticated()
                .and()

            // Enable Form Login for users.
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
