/*
 * Copyright 2018 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
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
package voyage.security.user

import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import spock.lang.Specification

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.Principal

class PasswordExpiredFilterSpec extends Specification {
    PasswordExpiredFilter filter
    UserService userService
    HttpServletRequest request
    HttpServletResponse response
    FilterChain filterChain

    void setup() {
        userService = Mock(UserService)
        filter = new PasswordExpiredFilter(userService)
        filter.excludeResources = ['/test/**', '/test2']
        filter.passwordResetDays = 90

        request = Mock(HttpServletRequest)
        response = Mock(HttpServletResponse)
        filterChain = Mock(FilterChain)
    }

    def 'Request (without path info) matches exclusion list and skips the filter work'() {
        when:
            filter.doFilter(request, response, filterChain)

        then:
            2 * request.servletPath >> '/test/something'
            0 * request.userPrincipal
            0 * userService.findByUsername(_)
            1 * filterChain.doFilter(request, response)
    }

    def 'Request (with path info) matches exclusion list and skips the filter work'() {
        when:
            filter.doFilter(request, response, filterChain)

        then:
            2 * request.servletPath >> '/test/something?paramA=1&paramB=2'
            0 * request.userPrincipal
            0 * userService.findByUsername(_)
            1 * filterChain.doFilter(request, response)
    }

    def 'Request is filterable but the user is not authenticated'() {
        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/filterable/request'
            1 * request.userPrincipal >> null
            0 * userService.findByUsername(_)
            1 * filterChain.doFilter(request, response)
    }

    def 'Request is filterable, the principal is authenticated, but not an instance of Authenticated'() {
        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/filterable/request'
            1 * request.userPrincipal >> Mock(Principal)
            0 * userService.findByUsername(_)
            1 * filterChain.doFilter(request, response)
    }

    def 'Request is filterable, has Authentication user token, but credentials are expired'() {
        given:
            Authentication userPrincipal = Mock(Authentication)
            UserDetails userDetails = Mock(UserDetails)
            PrintWriter responseWriter = Mock(PrintWriter)

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/filterable/request'
            1 * request.userPrincipal >> userPrincipal
            2 * userPrincipal.principal >> userDetails
            1 * userDetails.username >> 'test'
            1 * userService.findByUsername('test') >> new User(isCredentialsExpired:true, passwordCreatedDate:new Date() )
            1 * request.pathInfo
            1 * response.writer >> responseWriter
            1 * responseWriter.append('[{"error":"403_password_expired",' +
                    '"errorDescription":"Password is expired. Please change the password to access the application"}]')
            1 * responseWriter.close()
            1 * responseWriter.flush()
            0 * filterChain.doFilter(request, response)
    }

    def 'Request is filterable, has Authentication user token, but credentials are not expired'() {
        given:
            Authentication userPrincipal = Mock(Authentication)
            UserDetails userDetails = Mock(UserDetails)

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/filterable/request'
            1 * request.userPrincipal >> userPrincipal
            2 * userPrincipal.principal >> userDetails
            1 * userDetails.username >> 'test'
            1 * userService.findByUsername('test') >> new User(isCredentialsExpired:false, passwordCreatedDate:new Date() )
            1 * request.pathInfo
            1 * filterChain.doFilter(request, response)
    }

    def 'Logged in user credentials are not expired but date of password creation is more than expiration days'() {
        given:
            Authentication userPrincipal = Mock(Authentication)
            UserDetails userDetails = Mock(UserDetails)
            PrintWriter responseWriter = Mock(PrintWriter)

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/filterable/request'
            1 * request.userPrincipal >> userPrincipal
            2 * userPrincipal.principal >> userDetails
            1 * userDetails.username >> 'test'
            1 * userService.findByUsername('test') >> new User(isCredentialsExpired:false, passwordCreatedDate:new Date() - 100 )
            1 * request.pathInfo
            1 * response.writer >> responseWriter
            1 * responseWriter.append('[{"error":"403_password_expired",' +
                    '"errorDescription":"Password is expired. Please change the password to access the application"}]')
            1 * responseWriter.close()
            1 * responseWriter.flush()
            0 * filterChain.doFilter(request, response)
    }

    def 'Logged in user credentials are not expired and date of password creation is more than expiration days but passwordResetDays is 0'() {
        given:
            Authentication userPrincipal = Mock(Authentication)
            UserDetails userDetails = Mock(UserDetails)
            filter.passwordResetDays = 0

        when:
            filter.doFilter(request, response, filterChain)

        then:
            1 * request.servletPath >> '/filterable/request'
            1 * request.userPrincipal >> userPrincipal
            2 * userPrincipal.principal >> userDetails
            1 * userDetails.username >> 'test'
            1 * userService.findByUsername('test') >> new User(isCredentialsExpired:false, passwordCreatedDate:new Date() - 100 )
            1 * request.pathInfo
            1 * filterChain.doFilter(request, response)
    }
}
