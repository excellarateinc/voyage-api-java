package voyage.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import voyage.security.error.WebResponseExceptionTranslator

/**
 * Configures the OAuth2 Resource server which governs the API endpoints. Essentially this config injects the
 * OAuth2AuthenticationProcessingFilter into the servlet filter chain AND extends the HTTP Security policy that are
 * OAuth2 specific.
 *
 * NOTE: This config is limited to only handling RESOURCE authorizations extending from /api. All other web security
 * rules belong in the WebSecurityConfig, like authentication providers and other access permissions.
 */
//@Configuration
//@EnableResourceServer
class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    private static final String ANY_PATH = '/**'
    private static final String API_PATH = '/api/**'
    private static final String READ = "#oauth2.hasScope('Read Data')"
    private static final String WRITE = "#oauth2.hasScope('Write Data')"

    @Value('${security.permitAll}')
    private String[] permitAllUrls

    @Autowired
    private WebResponseExceptionTranslator apiWebResponseExceptionTranslator

    @Override
    void configure(HttpSecurity http) throws Exception {
        http

        // Limit this Config to only handle /api requests. This will also disable authentication filters on
        // /api requests and enable the OAuth2 token filter as the only means of stateless authentication.
                .requestMatchers()
                .antMatchers(API_PATH)
                .and()

        // Bypass URLs that are public endpoints, like /api/v1/forgotPassword
                .authorizeRequests()
                .antMatchers(permitAllUrls).permitAll()
                .and()

        // Enforce client 'scope' permissions on all authenticated requests
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, ANY_PATH).access(READ)
                .antMatchers(HttpMethod.POST, ANY_PATH).access(WRITE)
                .antMatchers(HttpMethod.PUT, ANY_PATH).access(WRITE)
                .antMatchers(HttpMethod.PATCH, ANY_PATH).access(WRITE)
                .antMatchers(HttpMethod.DELETE, ANY_PATH).access(WRITE)
                .and()
    }

    @Override
    void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources
        // Override exception formatting by injecting the accessDeniedHandler & authenticationEntryPoint
                .accessDeniedHandler(accessDeniedHandler())
                .authenticationEntryPoint(authenticationEntryPoint())
    }

    /**
     * Inject the custom exception translator into the Authentication Entry Point
     */
    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        OAuth2AuthenticationEntryPoint entryPoint = new OAuth2AuthenticationEntryPoint()
        entryPoint.setExceptionTranslator(apiWebResponseExceptionTranslator)
        return entryPoint
    }

    /**
     * Override the AccessDeniedHandler to use the custom API exception translator
     */
    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        OAuth2AccessDeniedHandler handler = new OAuth2AccessDeniedHandler()
        handler.setExceptionTranslator(apiWebResponseExceptionTranslator)
        return handler
    }
}

