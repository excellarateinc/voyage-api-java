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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsUtils
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Servlet filter that uses the currently authenticated user (if any) to determine which Origin's are allowed. The base
 * Spring Security CORS filter is very limited in that it accepts one Origin or all origins. When all origins are allowed
 * and "credentials" are allowed, then Spring Security echo's back the origin given to it. Echoing back request data
 * creates an opportunity for injection attacks, which is ultimately why this CorsServletFilter was created.
 *
 * Features
 * - if the 'client' requesting access to the API is authenticated
 *   - the given Origin is matched to the Client Origins in the database
 *   - if a match is found, then return the value in the database as the allowed origin
 *   - if no match is found, then default to being permissive and return a public wildcard allows for Origin
 * - if the request is anonymous
 *   - default to being permissive and return a public wildcard allows for Origin
 *
 * NOTE: Defaulting to permissive origin in this class because an assumption is made that the security framework will
 *       catch unauthorized requests and prevent access. For a more restrictive implementation, consider extending this
 *       class or replacing it with a different implementation.
 */
@Component
@Order(-10000000)
@ConditionalOnProperty(name = 'security.cors.enabled', havingValue = 'true')
class CorsFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(CorsFilter)
    private static final String HEADER_ORIGIN = 'Origin'
    private static final String HEADER_VARY = 'Vary'
    private static final String HEADER_ACCESS_ALLOW_HEADERS = 'Access-Control-Allow-Headers'
    private static final String HEADER_ACCESS_ALLOW_METHODS = 'Access-Control-Allow-Methods'
    private static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = 'Access-Control-Allow-Origin'
    private static final String HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS = 'Access-Control-Allow-Credentials'
    private static final String HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS_VALUE = 'true'
    private static final String HEADER_CORS_WILDCARD_VALUE = '*'

    @Value('${security.cors.access-control-allow-origins}')
    String[] accessControlAllowOrigins
    @Value('${security.cors.access-control-allow-headers}')
    String accessControlAllowHeaders
    @Value('${security.cors.access-control-allow-methods}')
    String accessControlAllowMethods

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (CorsUtils.isCorsRequest(request)) {
            boolean applied = applyOriginResponseHeaders(request, response)
            if (!applied || CorsUtils.isPreFlightRequest(request)) {
                return
            }
        } else {
            LOG.debug('CORS FILTER: Skipping CORS filtering for this request')
        }

        // Pass control to the next servlet filter.
        chain.doFilter(request, response)
    }

    private boolean applyOriginResponseHeaders(HttpServletRequest request, HttpServletResponse response) {
        String requestOrigin = request.getHeader(HEADER_ORIGIN)
        if (requestOrigin && isNotWildCardOrigin()) {

            String originMatch = accessControlAllowOrigins.find { clientOrigin ->
                cleanUri(clientOrigin) == cleanUri(requestOrigin)
            }
            if (originMatch) {
                writeRestrictedResponseHeaders(response, originMatch)
                return true
            }
        }
        writePublicResponseHeaders(response)
        return true
    }

    private boolean isNotWildCardOrigin() {
        return HEADER_CORS_WILDCARD_VALUE != accessControlAllowOrigins.first()
    }

    private void writeRestrictedResponseHeaders(HttpServletResponse response, String origin) {
        response.addHeader(HEADER_VARY, HEADER_ORIGIN)
        response.addHeader(HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, origin)
        response.addHeader(HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS, HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS_VALUE)
        response.addHeader(HEADER_ACCESS_ALLOW_HEADERS, accessControlAllowHeaders)
        response.addHeader(HEADER_ACCESS_ALLOW_METHODS, accessControlAllowMethods)
    }

    private void writePublicResponseHeaders(HttpServletResponse response) {
        response.addHeader(HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, HEADER_CORS_WILDCARD_VALUE)
        response.addHeader(HEADER_ACCESS_ALLOW_HEADERS, accessControlAllowHeaders)
        response.addHeader(HEADER_ACCESS_ALLOW_METHODS, accessControlAllowMethods)
    }

    private static String cleanUri(String uriIn) {
        String uri = uriIn.trim()
        if (uri.endsWith('/')) {
            int beforeTheSlash = uri.size() - 2  // remove the slash and account for a zero-based array
            uri = uri[0..beforeTheSlash]
        }
        return uri
    }
}
