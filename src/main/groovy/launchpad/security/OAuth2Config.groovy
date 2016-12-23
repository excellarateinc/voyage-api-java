package launchpad.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer

/**
 * @api {get} /oauth/token Request token
 * @apiVersion 1.0.0
 * @apiName Token
 * @apiGroup Authenticate
 *
 * @apiHeaderExample {json} Header-Example:
 * {
 *       "Accept": "application/json"
 * }
 * @apiParam {String} grant_type Specify the OAuth2 grant type of "client_credentials" or "authorization_code"
 *
 * @apiSuccess {String} access_token Authorization token
 * @apiSuccess {String} token_type Type of token (ie bearer)
 * @apiSuccess {String} expires_in Lifetime of the token in seconds
 * @apiSuccess {String} scope The access scope of the token
 *
 * @apiSuccessExample Success-Response:
 *   HTTP/1.1 200 OK
 *   [
 *       {
 *           "access_token": "caaafafd-08bb-4b83-b9cc-a3b78e500e91",
 *           "token_type": "bearer",
 *           "expires_in": 43020,
 *           "scope":"read"
 *       }
 *   ]
 *
 **/
@Configuration
@EnableAuthorizationServer
@EnableResourceServer
class OAuth2Config extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager

    @Override
    void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)
    }

    @Override
    void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.checkTokenAccess("isAuthenticated()")
    }

    @Override
    void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
            .withClient("my-trusted-client")
                .authorizedGrantTypes("password", "authorization_code", "refresh_token", "implicit")
                .authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT")
                .scopes("read", "write", "trust")
                .resourceIds("oauth2-resource")
                .accessTokenValiditySeconds(600)
            .and()
            .withClient("my-client-with-registered-redirect")
                .authorizedGrantTypes("authorization_code")
                .authorities("ROLE_CLIENT")
                .scopes("read", "trust")
                .resourceIds("oauth2-resource")
                .redirectUris("http://anywhere?key=value")
            .and()
            .withClient("my-client-with-secret")
                .authorizedGrantTypes("client_credentials", "password")
                .authorities("ROLE_CLIENT")
                .scopes("read")
                .resourceIds("oauth2-resource")
                .secret("secret")
    }

}