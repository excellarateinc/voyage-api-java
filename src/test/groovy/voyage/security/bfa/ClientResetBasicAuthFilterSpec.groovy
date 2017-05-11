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
package voyage.security.bfa

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import spock.lang.Specification
import voyage.security.client.Client
import voyage.security.client.ClientService

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

class ClientResetBasicAuthFilterSpec extends Specification {
    ClientResetBasicAuthFilter filter
    ClientService clientService

    HttpServletRequest request
    HttpServletResponse response
    HttpSession session
    FilterChain filterChain

    def setup() {
        request = Mock(HttpServletRequest)
        response = Mock(HttpServletResponse)
        filterChain = Mock(FilterChain)
        session = Mock(HttpSession)

        clientService = Mock(ClientService)

        filter = new ClientResetBasicAuthFilter(clientService)
        filter.isEnabled = true
        filter.resourcePaths = '/**'
    }

    def 'doFilterInternal is skipped if disabled'() {
        given:
            filter.isEnabled = false

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * filterChain.doFilter(request, response)
    }

    def 'doFilterInternal skips request if the url doesn not match'() {
        given:
            filter.isEnabled = true
            filter.resourcePaths = ['/nomatch']

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/api'
            1 * filterChain.doFilter(request, response)
    }

    def 'doFilterInternal finds a username with authenticated client and resets the failedLoginAttempts'() {
        given:
            filter.isEnabled = true
            filter.resourcePaths = ['/**']
            SecurityContext securityContext = Mock(SecurityContext)
            SecurityContextHolder.context = securityContext
            Authentication authentication = Mock(Authentication)
            User user = new User('client-super', 'password', [])

            Client client = new Client()
            client.failedLoginAttempts = 4

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/api'
            1 * request.getSession() >> session
            1 * request.getHeader('Authorization') >> 'Basic Y2xpZW50LXN1cGVyOnNlY3JldA=='
            1 * filterChain.doFilter(request, response)

            1 * securityContext.authentication >> authentication
            1 * authentication.isAuthenticated() >> true
            2 * authentication.principal >> user

            1 * clientService.findByClientIdentifier(user.username) >> client
            1 * clientService.save(_ as Client)

            client.failedLoginAttempts == 0
    }
}
