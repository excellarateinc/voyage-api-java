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
package voyage.security.filter

import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails
import spock.lang.Specification
import voyage.security.client.Client
import voyage.security.client.ClientService
import voyage.security.user.User
import voyage.security.user.UserService

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.Principal

class InvalidateOAuthTokensFilterSpec extends Specification {
    HttpServletRequest request
    HttpServletResponse response
    FilterChain filterChain
    InvalidateOAuthTokensFilter filter
    UserService userService
    ClientService clientService

    Client client
    User user
    Calendar tokenCreatedCal
    String tokenWithCreated = 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjcmVhdGVkIjoxNDkyODM0MTk3MzQ1LCJzY29wZSI6WyJSZWFk' +
            'IERhdGEiLCJXcml0ZSBEYXRhIl0sImV4cCI6MTQ5Mjg0MTM5NywiYXV0aG9yaXRpZXMiOlsiYXBpLnBlcm1pc3Npb25zLmRlbGV0ZSIsImF' +
            'waS5yb2xlcy5kZWxldGUiLCJhcGkucm9sZXMudXBkYXRlIiwiYXBpLnBlcm1pc3Npb25zLmxpc3QiLCJhcGkucGVybWlzc2lvbnMudXBkYX' +
            'RlIiwiYXBpLnVzZXJzLmNyZWF0ZSIsImFwaS51c2Vycy5nZXQiLCJhcGkudXNlcnMubGlzdCIsImFwaS5wZXJtaXNzaW9ucy5nZXQiLCJhc' +
            'Gkucm9sZXMuZ2V0IiwiYXBpLnVzZXJzLnVwZGF0ZSIsImFwaS5yb2xlcy5jcmVhdGUiLCJhcGkudXNlcnMuZGVsZXRlIiwiYXBpLnBlcm1p' +
            'c3Npb25zLmNyZWF0ZSIsImFwaS5yb2xlcy5saXN0Il0sImp0aSI6ImM2NDRhN2EwLThjMDktNDgzMC05NjIyLWQ0MWZiN2MwNDRkOCIsImN' +
            'saWVudF9pZCI6ImNsaWVudC1zdXBlciJ9.TwyS3n5fmJKIB08jJPnU6cxlz-dh1fCCrXStOwC0qNdzOH19XjXRpqzqXpnqtkTMkG4brDHvE' +
            'yT1qIv0YHHNddIrRvgKBXJMiTZrsN7pRg37e929HmlBTYHETYpJdNhiC7doenJGbNGexS6nuCZht7EXeVnXj4cSrO2iiMSjhyDDbhRxEWz0' +
            '1SdPvykAk0s1Xc_Bv_1dugRZHEw6UQRe3qjbzAC4cYSILANjAlphCkpIibOmk0TwuB2c9NaGuwo2xRHrcMNBG0bcpiCBlQyCmbf8uVvr5Th' +
            'szlh5FRmQSTqTUMgZKfFRgvliQTcS8bp2bIDUajQj5xHjCQlvzKlZMw'
    String tokenWithoutCreated = 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJSZWFkIERhdGEiLCJXcml0ZSBEYXRhIl0sI' +
            'mV4cCI6MTQ5MjYyMjYwNSwiYXV0aG9yaXRpZXMiOlsiYXBpLnBlcm1pc3Npb25zLmRlbGV0ZSIsImFwaS5yb2xlcy5kZWxldGUiLCJhcGku' +
            'cm9sZXMudXBkYXRlIiwiYXBpLnBlcm1pc3Npb25zLmxpc3QiLCJhcGkucGVybWlzc2lvbnMudXBkYXRlIiwiYXBpLnVzZXJzLmNyZWF0ZSI' +
            'sImFwaS51c2Vycy5nZXQiLCJhcGkudXNlcnMubGlzdCIsImFwaS5wZXJtaXNzaW9ucy5nZXQiLCJhcGkucm9sZXMuZ2V0IiwiYXBpLnVzZX' +
            'JzLnVwZGF0ZSIsImFwaS5yb2xlcy5jcmVhdGUiLCJhcGkudXNlcnMuZGVsZXRlIiwiYXBpLnBlcm1pc3Npb25zLmNyZWF0ZSIsImFwaS5yb' +
            '2xlcy5saXN0Il0sImp0aSI6ImYzZjk2MTQwLTdlZGQtNDAzYi1hNzM2LTZhYzhmMTYxZGE4OCIsImNsaWVudF9pZCI6ImNsaWVudC1zdXBl' +
            'ciJ9.Np25I-HuBGXKOJ-vNIBNwYfeR5mwtMaHOfmfh8MEv1lBNR_j5cVAcCkKCAdrlcWnjQHDCogo9X095XSyqZ0HN32YdKKcBoSXgLFHPf' +
            '4ZRUSwrPJrVTXg_E3TDmLR9EQcbY_nSVcSEtGW2rEqBpHdCG2EH_V7Ygv38Ujd5dTGA4nwJLb_Jxt2GOJip0bZGAGjXKXVDyrelA9b3B0qN' +
            'US5IVUerD4xdyGX9pQPFpQ03q0XlXJdRSbjm3ZXASmUodaLvQyHIdO68NBiW0cijaydtti-zsbocr9-1SvajUEyJyqpRD0cy7IS5--S_BPh' +
            'SmXCNG0o5pu8Ky0nS1Mw-awCzg'

    def setup() {
        request = Mock(HttpServletRequest)
        response = Mock(HttpServletResponse)
        filterChain = Mock(FilterChain)
        userService = Mock(UserService)
        clientService = Mock(ClientService)

        client = new Client()
        user = new User()

        tokenCreatedCal = Calendar.instance
        tokenCreatedCal.setTimeInMillis(1492834197345) // Value stored in the tokenWithCreated token

        filter = new InvalidateOAuthTokensFilter(clientService, userService)
    }

    def 'filter skips the request if no authenticated user is found'() {
        when:
            filter.doFilterInternal(request, response, filterChain)
        then:
            1 * request.userPrincipal >> null
            0 * clientService.currentClient
            0 * userService.currentUser
            1 * filterChain.doFilter(request, response)
            0 * response.writer >> Mock(PrintWriter)
    }

    def 'filter skips the request if the authenticated user is not OAuth2Authentication'() {
        when:
            filter.doFilterInternal(request, response, filterChain)
        then:
            1 * request.userPrincipal >> Mock(Principal)
            0 * clientService.currentClient
            0 * userService.currentUser
            1 * filterChain.doFilter(request, response)
            0 * response.writer >> Mock(PrintWriter)
    }

    def 'filter throws an exception if the request does not contain a `created` attribute'() {
        given:
            OAuth2Authentication principal = Mock(OAuth2Authentication)
            OAuth2AuthenticationDetails details = Mock(OAuth2AuthenticationDetails)
        when:
            filter.doFilterInternal(request, response, filterChain)
        then:
            1 * request.userPrincipal >> principal
            1 * principal.details >> details
            1 * details.tokenValue >> tokenWithoutCreated
            0 * clientService.currentClient
            0 * userService.currentUser
            0 * filterChain.doFilter(request, response)
            thrown(UnsupportedOperationException)
    }

    def 'filter returns an error response because the Client is disabled'() {
        given:
            OAuth2Authentication principal = Mock(OAuth2Authentication)
            OAuth2AuthenticationDetails details = Mock(OAuth2AuthenticationDetails)
            client.isEnabled = false
        when:
            filter.doFilterInternal(request, response, filterChain)
        then:
            1 * request.userPrincipal >> principal
            1 * principal.details >> details
            1 * details.tokenValue >> tokenWithCreated
            1 * clientService.currentClient >> client
            0 * userService.currentUser
            0 * filterChain.doFilter(request, response)
            1 * response.writer >> Mock(PrintWriter)
    }

    def 'filter returns an error response because the token is older than the Client.forceTokensExpiredDate'() {
        given:
            OAuth2Authentication principal = Mock(OAuth2Authentication)
            OAuth2AuthenticationDetails details = Mock(OAuth2AuthenticationDetails)

            Calendar forceTokensExpired = Calendar.instance
            forceTokensExpired.setTimeInMillis(tokenCreatedCal.timeInMillis)
            forceTokensExpired.add(Calendar.HOUR, 1)
            client.forceTokensExpiredDate = forceTokensExpired.time
        when:
            filter.doFilterInternal(request, response, filterChain)
        then:
            1 * request.userPrincipal >> principal
            1 * principal.details >> details
            1 * details.tokenValue >> tokenWithCreated
            1 * clientService.currentClient >> client
            0 * userService.currentUser
            0 * filterChain.doFilter(request, response)
            1 * response.writer >> Mock(PrintWriter)
    }

    def 'filter skips through the service because the Client does not have a forcedTokensExpired date'() {
        given:
            OAuth2Authentication principal = Mock(OAuth2Authentication)
            OAuth2AuthenticationDetails details = Mock(OAuth2AuthenticationDetails)
        when:
            filter.doFilterInternal(request, response, filterChain)
        then:
            1 * request.userPrincipal >> principal
            1 * principal.details >> details
            1 * details.tokenValue >> tokenWithCreated
            1 * clientService.currentClient >> client
            1 * userService.currentUser >> null
            1 * filterChain.doFilter(request, response)
            0 * response.writer >> Mock(PrintWriter)
    }

    def 'filter skips through the service because the token was created after the Client.forcedTokensExpired date'() {
        given:
            OAuth2Authentication principal = Mock(OAuth2Authentication)
            OAuth2AuthenticationDetails details = Mock(OAuth2AuthenticationDetails)

            Calendar forceTokensExpired = Calendar.instance
            forceTokensExpired.setTimeInMillis(tokenCreatedCal.timeInMillis)
            forceTokensExpired.add(Calendar.HOUR, -1)
            client.forceTokensExpiredDate = forceTokensExpired.time
        when:
            filter.doFilterInternal(request, response, filterChain)
        then:
            1 * request.userPrincipal >> principal
            1 * principal.details >> details
            1 * details.tokenValue >> tokenWithCreated
            1 * clientService.currentClient >> client
            1 * userService.currentUser >> null
            1 * filterChain.doFilter(request, response)
            0 * response.writer >> Mock(PrintWriter)
    }

    def 'filter returns an error response because the User is disabled'() {
        given:
            OAuth2Authentication principal = Mock(OAuth2Authentication)
            OAuth2AuthenticationDetails details = Mock(OAuth2AuthenticationDetails)
            user.isEnabled = false
        when:
            filter.doFilterInternal(request, response, filterChain)
        then:
            1 * request.userPrincipal >> principal
            1 * principal.details >> details
            1 * details.tokenValue >> tokenWithCreated
            1 * clientService.currentClient >> client
            1 * userService.currentUser >> user
            0 * filterChain.doFilter(request, response)
            1 * response.writer >> Mock(PrintWriter)
    }

    def 'filter returns an error response because the User account is expired'() {
        given:
            OAuth2Authentication principal = Mock(OAuth2Authentication)
            OAuth2AuthenticationDetails details = Mock(OAuth2AuthenticationDetails)
            user.isAccountExpired = true
        when:
            filter.doFilterInternal(request, response, filterChain)
        then:
            1 * request.userPrincipal >> principal
            1 * principal.details >> details
            1 * details.tokenValue >> tokenWithCreated
            1 * clientService.currentClient >> client
            1 * userService.currentUser >> user
            0 * filterChain.doFilter(request, response)
            1 * response.writer >> Mock(PrintWriter)
    }

    def 'filter returns an error response because the User account is locked'() {
        given:
            OAuth2Authentication principal = Mock(OAuth2Authentication)
            OAuth2AuthenticationDetails details = Mock(OAuth2AuthenticationDetails)
            user.isAccountLocked = true
        when:
            filter.doFilterInternal(request, response, filterChain)
        then:
            1 * request.userPrincipal >> principal
            1 * principal.details >> details
            1 * details.tokenValue >> tokenWithCreated
            1 * clientService.currentClient >> client
            1 * userService.currentUser >> user
            0 * filterChain.doFilter(request, response)
            1 * response.writer >> Mock(PrintWriter)
    }

    def 'filter returns an error response because the token is older than the User.forceTokensExpiredDate'() {
        given:
            OAuth2Authentication principal = Mock(OAuth2Authentication)
            OAuth2AuthenticationDetails details = Mock(OAuth2AuthenticationDetails)

            Calendar forceTokensExpired = Calendar.instance
            forceTokensExpired.setTimeInMillis(tokenCreatedCal.timeInMillis)
            forceTokensExpired.add(Calendar.HOUR, 1)
            user.forceTokensExpiredDate = forceTokensExpired.time
        when:
            filter.doFilterInternal(request, response, filterChain)
        then:
            1 * request.userPrincipal >> principal
            1 * principal.details >> details
            1 * details.tokenValue >> tokenWithCreated
            1 * clientService.currentClient >> client
            1 * userService.currentUser >> user
            0 * filterChain.doFilter(request, response)
            1 * response.writer >> Mock(PrintWriter)
    }

    def 'filter skips through the service because the User does not have a forcedTokensExpired date'() {
        given:
            OAuth2Authentication principal = Mock(OAuth2Authentication)
            OAuth2AuthenticationDetails details = Mock(OAuth2AuthenticationDetails)
        when:
            filter.doFilterInternal(request, response, filterChain)
        then:
            1 * request.userPrincipal >> principal
            1 * principal.details >> details
            1 * details.tokenValue >> tokenWithCreated
            1 * clientService.currentClient >> client
            1 * userService.currentUser >> user
            1 * filterChain.doFilter(request, response)
            0 * response.writer >> Mock(PrintWriter)
    }

    def 'filter skips through the service because the token was created after the User.forcedTokensExpired date'() {
        given:
            OAuth2Authentication principal = Mock(OAuth2Authentication)
            OAuth2AuthenticationDetails details = Mock(OAuth2AuthenticationDetails)

            Calendar forceTokensExpired = Calendar.instance
            forceTokensExpired.setTimeInMillis(tokenCreatedCal.timeInMillis)
            forceTokensExpired.add(Calendar.HOUR, -1)
            user.forceTokensExpiredDate = forceTokensExpired.time
        when:
            filter.doFilterInternal(request, response, filterChain)
        then:
            1 * request.userPrincipal >> principal
            1 * principal.details >> details
            1 * details.tokenValue >> tokenWithCreated
            1 * clientService.currentClient >> client
            1 * userService.currentUser >> user
            1 * filterChain.doFilter(request, response)
            0 * response.writer >> Mock(PrintWriter)
    }
}
