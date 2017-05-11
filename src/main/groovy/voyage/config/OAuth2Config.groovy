/*
 * Copyright 2017 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package voyage.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import voyage.security.PermissionBasedClientDetailsService
import voyage.security.crypto.KeyStoreService
import voyage.security.error.WebResponseExceptionTranslator

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
        @Value('${security.jwt.private-key-name}')
        private String privateKeyName

        @Value('${security.jwt.private-key-password}')
        private String privateKeyPassword

        @Autowired
        private KeyStoreService keyStoreService

        @Autowired
        private AuthenticationManager authenticationManager

        @Autowired
        private PermissionBasedClientDetailsService permissionBasedClientDetailsService

        @Autowired
        private WebResponseExceptionTranslator apiWebResponseExceptionTranslator

        @Bean
        JwtAccessTokenConverter accessTokenConverter() {
            KeyPair keyPair = keyStoreService.getRsaKeyPair(privateKeyName, privateKeyPassword.toCharArray())
            JwtAccessTokenConverter converter = new TimestampJwtAccessTokenConverter()
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
            endpoints
                    .authenticationManager(authenticationManager)
                    .accessTokenConverter(accessTokenConverter())
                    .exceptionTranslator(apiWebResponseExceptionTranslator)
        }

        @Override
        void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.withClientDetails(permissionBasedClientDetailsService)
        }

        class TimestampJwtAccessTokenConverter extends JwtAccessTokenConverter {
            @Override
            OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
                DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken)accessToken
                if (!token.additionalInformation) {
                    Map<String, Object> additionalInfo = [:]
                    token.setAdditionalInformation(additionalInfo)
                }
                token.additionalInformation.put('created', Calendar.instance.timeInMillis)
                return super.enhance(accessToken, authentication)
            }
        }
    }

    /**
     * Configures the OAuth2 Resource server which governs the API endpoints. Essentially this config injects the
     * OAuth2AuthenticationProcessingFilter into the servlet filter chain AND extends the HTTP Security policy that are
     * OAuth2 specific.
     *
     * NOTE: This config is limited to only handling RESOURCE authorizations extending from /api. All other web security
     * rules belong in the WebSecurityConfig, like authentication providers and other access permissions.
     */
    @Configuration
    @EnableResourceServer
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
}
