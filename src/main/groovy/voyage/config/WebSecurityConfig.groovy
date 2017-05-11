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
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import voyage.security.PermissionBasedUserDetailsService
import voyage.security.crypto.CryptoService

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
    private static final String LOGIN_PATH = '/login'

    @Value('${security.ignored}')
    private String[] ignoredUrls

    @Value('${security.permitAll}')
    private String[] permitAllUrls

    @Autowired
    private PermissionBasedUserDetailsService permissionBasedUserDetailsService

    @Autowired
    void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth

            // Register the custom Permission Based User Details Service
            .userDetailsService(permissionBasedUserDetailsService)

            // Override the default password encoder
            .passwordEncoder(CryptoService.PASSWORD_ENCODER)
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
                .antMatchers(permitAllUrls).permitAll()
                .and()

            // Enforce every request to be authenticated
            .authorizeRequests()
                .anyRequest().authenticated()
                .and()

            // Enable Form Login for users.
            .formLogin()
                .loginPage(LOGIN_PATH).permitAll()
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
            .ignoring()
            .antMatchers(ignoredUrls)
    }
}
