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

import groovy.json.JsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.jwt.JwtHelper
import org.springframework.security.oauth2.common.util.JsonParser
import org.springframework.security.oauth2.common.util.JsonParserFactory
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import voyage.security.client.Client
import voyage.security.client.ClientService
import voyage.security.user.User
import voyage.security.user.UserService

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.Principal

/**
 * Servlet filter that parses the incoming OAuth2 token for a "created" date and compares that date against a forced
 * expiration date set on either the Client or User accounts associated with the token. This servlet filter will also
 * check to see if the Client or User profiles have been locked or disabled in any way and will return a token expired
 * message in these cases.
 */
@Component
class InvalidateOAuthTokensFilter extends OncePerRequestFilter  {
    private static final Logger LOG = LoggerFactory.getLogger(InvalidateOAuthTokensFilter)
    private final JsonParser objectMapper = JsonParserFactory.create()
    private final ClientService clientService
    private final UserService userService

    @Autowired
    InvalidateOAuthTokensFilter(ClientService clientService, UserService userService) {
        this.clientService = clientService
        this.userService = userService
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        LOG.debug('doFilterInternal(): Checking the incoming user token to see if it is valid')

        Principal principal = request.userPrincipal
        if (principal && principal instanceof OAuth2Authentication) {
            OAuth2Authentication auth = (OAuth2Authentication)principal
            String token = getToken(auth)
            Date tokenCreatedDate = getTokenCreatedDate(token)

            if (tokenCreatedDate) {
                Client client = clientService.currentClient
                if (!isClientTokenValid(client, tokenCreatedDate)) {
                    writeErrorResponse(response)
                    return
                }

                User user = userService.currentUser
                if (user && !isUserTokenValid(user, tokenCreatedDate)) {
                    writeErrorResponse(response)
                    return
                }

            } else {
                LOG.error('doFilterInternal(): The `created` timestamp was not found on the token ' + token)
                throw new UnsupportedOperationException('The authentication token provided does not contain a created date.')
            }

        } else if (!principal) {
            LOG.debug('doFilterInternal(): The request.userPrincipal is null. Not an authenticated user. Skipping analysis.')

        } else {
            LOG.debug('doFilterInternal(): The request.userPrincipal is not an OAuth2Authentication object. Skipping analysis. ' +
                    'User Principal=' + principal)
        }

        // Pass control to the next servlet filter.
        chain.doFilter(request, response)
    }

    private static boolean isClientTokenValid(Client client, Date tokenCreatedDate) {
        if (client && client.isEnabled &&
                (!client.forceTokensExpiredDate || tokenCreatedDate.after(client.forceTokensExpiredDate))) {
            if (LOG.debugEnabled) {
                LOG.debug('isClientTokenValid(): Token and client record is valid for clientId=' + client.id)
            }
            return true
        }
        if (LOG.debugEnabled) {
            LOG.debug('isClientTokenValid(): Token or the client record is NOT valid for clientId=' + client?.id)
        }
        return false
    }

    private static boolean isUserTokenValid(User user, Date tokenCreatedDate) {
        if (user && user.isEnabled && !user.isAccountExpired && !user.isAccountLocked &&
                (!user.forceTokensExpiredDate || tokenCreatedDate.after(user.forceTokensExpiredDate))) {
            if (LOG.debugEnabled) {
                LOG.debug('isUserTokenValid(): Token and user record is valid for userId=' + user.id)
            }
            return true
        }
        if (LOG.debugEnabled) {
            LOG.debug('isUserTokenValid(): Token or the user record is NOT valid for userId=' + user?.id)
        }
        return false
    }

    private static String getToken(OAuth2Authentication auth) {
        String token = ((OAuth2AuthenticationDetails)auth.details).tokenValue
        if (LOG.debugEnabled) {
            if (token) {
                LOG.debug("getToken(): Found token for ${auth.principal}. Token=${token}")
            } else {
                LOG.debug("getToken(): No token found for ${auth.principal}.")
            }
        }
        return token
    }

    private Date getTokenCreatedDate(String token) {
        Map<String, Object> additionalInfo = decodeToken(token)
        Object tokenCreatedValue = additionalInfo['created']
        if (tokenCreatedValue) {
            Calendar tokenCreatedDate = Calendar.instance
            tokenCreatedDate.setTimeInMillis((long)tokenCreatedValue)
            if (LOG.debugEnabled) {
                LOG.debug("getTokenCreatedDate(): Token was created on ${tokenCreatedDate.format('yyyy-MM-dd\'T\'HH:mm:ssZ')}. Toke=${token}")
            }
            return tokenCreatedDate.time
        }
        if (LOG.debugEnabled) {
            LOG.debug("getTokenCreatedDate(): Token does NOT contain a 'created' property. Token=${token}")
        }
        return null
    }

    private Map<String, Object> decodeToken(String token) {
        String tokenClaims = JwtHelper.decode(token)?.claims
        return objectMapper.parseMap(tokenClaims)
    }

    private static void writeErrorResponse(HttpServletResponse response) {
        LOG.debug('writeErrorResponse(): Returning 401_unauthorized token expired message in the response.')
        Map errorResponse = [
            error:'401_unauthorized',
            errorDescription:'Access token expired',
        ]
        JsonBuilder json = new JsonBuilder([errorResponse])

        response.contentType = 'application/json'
        response.status = HttpStatus.UNAUTHORIZED.value()
        Writer responseWriter = response.writer
        json.writeTo(responseWriter)
        responseWriter.close()
        responseWriter.flush()
    }
}
