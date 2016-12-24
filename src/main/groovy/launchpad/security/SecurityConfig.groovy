package launchpad.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
class SecurityConfig extends WebSecurityConfigurerAdapter {

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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // Enforce authorization on any incoming request and required authentication to access any resources
            .authorizeRequests().anyRequest().authenticated().and()

            // Enable HTTP Basic Authentication as a means to accept a username and password
            .httpBasic().and()

            // Enable HTML form login page as a means to accept a username and password
            .formLogin().and()

            // Enable logging out (primarily for manual testing purposes).
            // - Bypass CSRF by adding a request matcher since this is a REST API. CSRF is only used on login.
            // TODO Lock this down to only the TEST environment
            .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
    }

    @Override
    void configure(WebSecurity web) throws Exception {
        web
            // Ignore authentication requirements on the following URL endpoints
            .ignoring()
            .antMatchers("/hello")
            .antMatchers("/oauth/*")
    }
}
