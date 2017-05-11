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
package voyage.security.audit

import spock.lang.Specification
import voyage.security.client.Client
import voyage.security.client.ClientService
import voyage.security.user.User
import voyage.security.user.UserService

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.Principal

class HttpActionLogAuthFilterSpec extends Specification {
    def 'doFilterInternal sets attributes for user and client'() {
        given:
            User user = new User()
            Client client = new Client()

            UserService userService = Mock(UserService)
            ClientService clientService = Mock(ClientService)

            HttpServletRequest request = Mock(HttpServletRequest)
            HttpServletResponse response = Mock(HttpServletResponse)
            FilterChain filterChain = Mock(FilterChain)

            HttpActionLogAuthFilter filter = new HttpActionLogAuthFilter(userService, clientService)

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * userService.currentUser >> user
            1 * clientService.currentClient >> client
            1 * request.userPrincipal >> {
                Principal principal = Mock(Principal)
                principal.name >> 'test-user'
                return principal
            }

            1 * request.setAttribute(HttpActionLogFilter.USER_PRINCIPAL_KEY, 'test-user')
            1 * request.setAttribute(HttpActionLogFilter.USER_KEY, user)
            1 * request.setAttribute(HttpActionLogFilter.CLIENT_KEY, client)

            1 * filterChain.doFilter(request, response)
    }
}
