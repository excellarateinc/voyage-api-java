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

import groovy.json.JsonException
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.security.crypto.codec.Base64
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import voyage.security.client.Client
import voyage.security.client.ClientService
import voyage.security.user.User
import voyage.security.user.UserService

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Filter that intercepts the incoming request and outgoing response and logs all relevant data to the database. Since
 * the Spring Security authentication data is handled (and cleared) further down the filter chain, the HttpActionLogAuthFilter
 * helper filter is ordered towards the bottom to capture authentication information and store the data into request
 * attributes for reference by the save response task in this filter.
 */
@Component
@Order(-1000)
class HttpActionLogFilter extends OncePerRequestFilter {
    private static final LOG = LoggerFactory.getLogger(HttpActionLogFilter)
    private static final String UNKNOWN = 'unknown'
    private static final String MASKED_VALUE = '*********'
    private static final int MAX_PAYLOAD_LENGTH = 10000
    private static final String CHARSET = 'UTF-8'
    private static final String DELIMITER_COLON = ':'
    private static final String DELIMITER_COMMA = ', '

    public static final String USER_KEY = 'HTTP_ACTION_LOG_USER'
    public static final String CLIENT_KEY = 'HTTP_ACTION_LOG_CLIENT'
    public static final String USER_PRINCIPAL_KEY = 'HTTP_ACTION_LOG_USER_PRINCIPAL'

    @Value('${security.http-audit-log.exclude-resources}')
    private String[] excludeResourcePaths

    @Value('${security.http-audit-log.mask-fields}')
    private String[] maskFields

    @Value('${security.http-audit-log.form-username-fields}')
    private String[] formUsernameFields

    @Value('${security.http-audit-log.store-request-body}')
    private boolean isStoreRequestBody

    @Value('${security.http-audit-log.store-response-body}')
    private boolean isStoreResponseBody

    private final ActionLogService actionLogService
    private final UserService userService
    private final ClientService clientService

    @Autowired
    HttpActionLogFilter(ActionLogService actionLogService, UserService userService, ClientService clientService) {
        this.actionLogService = actionLogService
        this.userService = userService
        this.clientService = clientService
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isRequestFilterable(request)) {
            long startTimeMs = System.currentTimeMillis()
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request)
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response)

            // Save the request
            ActionLog actionLog = saveRequest(wrappedRequest)

            // Complete the request
            filterChain.doFilter(wrappedRequest, wrappedResponse)

            // Save the response
            saveResponse(wrappedRequest, wrappedResponse, actionLog, startTimeMs)

            // Copy content of response back into original response
            wrappedResponse.copyBodyToResponse()

        } else {
            filterChain.doFilter(request, response)
        }
    }

    private ActionLog saveRequest(HttpServletRequest request) {
        ActionLog actionLog = new ActionLog()
        actionLog.with {
            clientIpAddress = getClientIpAddress(request)
            clientProtocol = getClientProtocol(request)
            httpMethod = request.method
            requestHeaders = getHeaders(request)
            url = request.requestURL
        }
        if (request.queryString) {
            actionLog.url += '?' + request.queryString
        }
        return actionLogService.saveDetached(actionLog)
    }

    private ActionLog saveResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
                                   ActionLog actionLog, Long startTimeMs) {
        User authenticatedUser = (User)request.getAttribute(USER_KEY)
        Client authenticatedClient = (Client)request.getAttribute(CLIENT_KEY)
        String userPrincipal = getUserPrincipal(request)

        actionLog.with {
            // Developers Note: Sometimes can be zero because of inaccurate system time, https://blogs.msdn.microsoft.com/ericlippert/2010/04/08/precision-and-accuracy-of-datetime/
            durationMs = System.currentTimeMillis() - startTimeMs
            username = userPrincipal
            user = authenticatedUser
            client = authenticatedClient
            httpStatus = response.status
            responseHeaders = getHeaders(response)
        }

        if (isStoreRequestBody) {
            actionLog.requestBody = getBody(request.contentType, request.contentAsByteArray, request.characterEncoding)
        }

        if (isStoreResponseBody) {
            actionLog.responseBody = getBody(response.contentType, response.contentAsByteArray, response.characterEncoding)
        }

        actionLogService.saveDetached(actionLog)
    }

    private String getUserPrincipal(HttpServletRequest request) {
        String userPrincipal = (String)request.getAttribute(USER_PRINCIPAL_KEY)

        // Check the basic auth header for a username if basic authentication failed
        if (!userPrincipal) {
            userPrincipal = lookupBasicAuthUsername(request)
        }

        // Check the request parameters for a username if form authentication failed
        if (!userPrincipal) {
            for (formUsernameField in formUsernameFields) {
                userPrincipal = request.getParameter(formUsernameField)
                if (userPrincipal) {
                    break
                }
            }
        }

        return userPrincipal
    }

    private String getBody(String contentType, byte[] buf, String encoding) {
        if (buf == null || buf.length == 0) {
            return ''
        }
        int length = Math.min(buf.length, MAX_PAYLOAD_LENGTH)
        try {
            String content = new String(buf, 0, length, encoding)
            return filterRequestBody(contentType, content, encoding)
        } catch (UnsupportedEncodingException ignore) {
            return 'Unsupported Encoding'
        }
    }

    private static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader('X-Forwarded-For')
        if (!ip || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.remoteAddr
        }
        return ip
    }

    private static String getClientProtocol(HttpServletRequest request) {
        String ip = request.getHeader('X-Forwarded-Proto')
        if (!ip || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.protocol
        }
        return ip
    }

    private String getHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder()
        request.headerNames?.each { name ->
            if (headers.length() > 0) {
                headers.append(DELIMITER_COMMA)
            }
            appendMasked(headers, name, request.getHeader(name), DELIMITER_COLON)

        }
        return headers.toString()
    }

    private String getHeaders(HttpServletResponse response) {
        StringBuilder headers = new StringBuilder()
        response.headerNames?.each { name ->
            if (headers.length() > 0) {
                headers.append(DELIMITER_COMMA)
            }
            appendMasked(headers, name, response.getHeader(name), DELIMITER_COLON)
        }
        return headers.toString()
    }

    private String filterRequestBody(String contentType, String httpContent, String encoding) {
        String body = httpContent
        if (contentType?.equalsIgnoreCase('application/x-www-form-urlencoded')) {
            String url = 'http://placeholder?' + httpContent
            List<NameValuePair> params
            try {
                params = URLEncodedUtils.parse(new URI(url), encoding)
            } catch (URISyntaxException ignore) {
                if (LOG.debugEnabled) {
                    LOG.debug('filterRequestBody(): Could not parse url ' + url)
                }
            }
            if (params) {
                StringBuilder output = new StringBuilder()
                for (NameValuePair param : params) {
                    if (output.length() > 0) {
                        output.append('&')
                    }
                    if (param.value) {
                        appendMasked(output, param.name, param.value, '=')
                    } else {
                        output.append(param.name)
                    }
                }
                body = output.toString()
            }
        } else if (contentType?.equalsIgnoreCase('application/json')) {
            try {
                body = JsonUtil.replaceAll(httpContent, maskFields, MASKED_VALUE)
            } catch (JsonException ignore) {
                if (LOG.debugEnabled) {
                    LOG.debug('filterRequestBody(): Could not parse JSON ' + httpContent)
                }
            }
        }
        return body
    }

    private StringBuilder appendMasked(StringBuilder builder, String key, String value, String delimiter) {
        builder.append(key).append(delimiter)
        boolean isMaskedField = maskFields.find { fieldName ->
            fieldName.equalsIgnoreCase(key)
        }
        if (isMaskedField) {
            builder.append(MASKED_VALUE)
        } else {
            builder.append(value)
        }
    }

    protected boolean isRequestFilterable(HttpServletRequest request) {
        String path = getRequestPath(request)
        AntPathMatcher antPathMatcher = new AntPathMatcher()
        for (String antPattern : excludeResourcePaths) {
            if (antPathMatcher.match(antPattern, path)) {
                if (LOG.debugEnabled) {
                    LOG.debug("Request path ${path} is excluded from this filter. Skipping.")
                }
                return false
            }
        }
        if (LOG.debugEnabled) {
            LOG.debug("Request path ${path} is filterable")
        }
        return true
    }

    protected static String getRequestPath(HttpServletRequest request) {
        String url = request.servletPath
        if (request.pathInfo) {
            url += request?.pathInfo
        }
        return url
    }

    private static String lookupBasicAuthUsername(HttpServletRequest request) {
        LOG.debug('Looking for a username in Basic Auth header')
        String username = null
        String header = request.getHeader('Authorization')
        if (header?.startsWith('Basic ')) {
            if (LOG.debugEnabled) {
                LOG.debug('Found Basic Auth header: ' + header)
            }
            byte[] base64Token = header[6..-1].getBytes(CHARSET)
            byte[] decoded
            try {
                decoded = Base64.decode(base64Token)
            } catch (IllegalArgumentException ignore) {
                if (LOG.debugEnabled) {
                    LOG.debug('Could not decode the Basic Auth header value: ' + header)
                }
            }
            if (decoded) {
                String token = new String(decoded, CHARSET)
                if (LOG.debugEnabled) {
                    LOG.debug('Decoded Basic Auth token: ' + token)
                }
                int delimiter = token.indexOf(DELIMITER_COLON)
                if (delimiter > -1) {
                    username = token[0..delimiter - 1]
                    if (LOG.debugEnabled) {
                        LOG.debug('Found username: ' + username)
                    }
                } else {
                    if (LOG.debugEnabled) {
                        LOG.debug('Could not extract username from token: ' + token)
                    }
                }
            }
        } else {
            if (LOG.debugEnabled) {
                LOG.debug('Authorization header is not Basic Auth. Skipping.')
            }
        }
        return username
    }
}
