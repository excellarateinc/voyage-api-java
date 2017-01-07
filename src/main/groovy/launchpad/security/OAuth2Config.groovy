package launchpad.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory

import java.security.KeyPair

@Configuration
class OAuth2Config {

    /**
     * Configures the OAuth2 Authorization server to use a custom ClientDetailsService and to govern access to
     * authorization endpoints.
     */
    @Configuration
    @EnableAuthorizationServer
    class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

        @Value('${security.jwt.key-store-filename}')
        private String keyStoreFileName

        @Value('${security.jwt.key-store-password}')
        private String keyStorePassword

        @Value('${security.jwt.private-key-name}')
        private String privateKeyName

        @Value('${security.jwt.private-key-password}')
        private String privateKeyPassword

        @Autowired
        private AuthenticationManager authenticationManager

        @Autowired
        private PermissionBasedClientDetailsService permissionBasedClientDetailsService

        @Bean
        JwtAccessTokenConverter accessTokenConverter() {
            KeyStoreKeyFactory keyFactory = new KeyStoreKeyFactory(new ClassPathResource(keyStoreFileName), keyStorePassword.toCharArray())
            KeyPair keyPair = keyFactory.getKeyPair(privateKeyName, privateKeyPassword.toCharArray())
            JwtAccessTokenConverter converter = new JwtAccessTokenConverter()
            converter.keyPair = keyPair
            return converter
        }

        @Override
        void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
            oauthServer

                // Expose the verifier key endpoint "/oauth/token_key" to the public for validation of the JWT token
                .tokenKeyAccess('permitAll()')

                // Require users to be authenticated before accessing "/oauth/check_token"
                .checkTokenAccess('isAuthenticated()')
        }

        @Override
        void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints.authenticationManager(authenticationManager).accessTokenConverter(accessTokenConverter())
        }

        @Override
        void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.withClientDetails(permissionBasedClientDetailsService)
        }
    }

    /**
     * Configures the OAuth2 Resource server which governs the API endpoints. Essentially this config injects the
     * OAuth2AuthenticationProcessingFilter into the servlet filter chain AND extends the HTTP Security policy that are
     * OAuth2 specific.
     *
     * It's important to know that the ResourceServerConfig is ordered ahead of the WebSecurityConfig, so whatever is
     * configured in this class for http authorizations will be executed BEFORE the rules defined in WebSecurityConfig.
     */
    @Configuration
    @EnableResourceServer
    class ResourceServerConfig extends ResourceServerConfigurerAdapter {
        @Override
        void configure(HttpSecurity http) throws Exception {
            http

                // Allow any user to access 'login' and web 'resources' like CSS/JS
                .authorizeRequests()
                    .antMatchers('/resources/**', 'login').permitAll()
                    .and()

                // Enforce every request to be authenticated
//                .authorizeRequests()
//                    .anyRequest().authenticated()
//                    .and()

                // Force any request to /oauth/authorize to require an authenticated user. This will essentially redirect
                // to the user login page.
                .authorizeRequests()
                    .antMatchers('/oauth/authorize').authenticated().and()

                // Enforce client 'scope' permissions once authenticated
                .authorizeRequests()
                    .antMatchers(HttpMethod.GET, '/**').access("#oauth2.hasScope('Read_Data')")
                    .antMatchers(HttpMethod.POST, '/**').access("#oauth2.hasScope('Write_Data')")
                    .antMatchers(HttpMethod.PUT, '/**').access("#oauth2.hasScope('Write_Data')")
                    .antMatchers(HttpMethod.DELETE, '/**').access("#oauth2.hasScope('Write_Data')")
                    .and()

                // Required to process form-based login
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and()


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
                .httpBasic()
        }
    }
}
