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

import org.owasp.esapi.ESAPI
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse
import java.util.regex.Pattern

/**
 * Servlet filter that parses all incoming request parameters and header values for malicious XSS attacks. Any request
 * content that matches the XSS PATTERNS are removed from the request before they are accessed by the API. Additionally,
 * this filter will encode all incoming data by default to avoid encoding attacks for HTML, CSS, JS, SQL, VBScript, OS,
 * LDAP, XPATH, XML, URL.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class XssFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(XssFilter)

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        LOG.debug('XssServletFilter: Wrapping the inbound request with the XSSRequestWrapper.')
        chain.doFilter(getXssRequestWrapperInstance((HttpServletRequest) request), response)
    }

    XssRequestWrapper getXssRequestWrapperInstance(HttpServletRequest request) {
        return new XssRequestWrapper(request)
    }

    class XssRequestWrapper extends HttpServletRequestWrapper {
        private static final Pattern[] PATTERNS = [
                // Script fragments
                Pattern.compile('<script>(.*?)</script>', Pattern.CASE_INSENSITIVE),
                // src='...'
                Pattern.compile('src[\r\n]*=[\r\n]*\'(.*?)\'', Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
                Pattern.compile('src[\r\n]*=[\r\n]*\"(.*?)\"', Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
                // lonely script tags
                Pattern.compile('</script>', Pattern.CASE_INSENSITIVE),
                Pattern.compile('<script(.*?)>', Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
                // eval(...)
                Pattern.compile('eval\\((.*?)\\)', Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
                // expression(...)
                Pattern.compile('expression\\((.*?)\\)', Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
                // javascript:...
                Pattern.compile('javascript:', Pattern.CASE_INSENSITIVE),
                // vbscript:...
                Pattern.compile('vbscript:', Pattern.CASE_INSENSITIVE),
                // onload(...)=...
                Pattern.compile('onload(.*?)=', Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        ]

        XssRequestWrapper(HttpServletRequest servletRequest) {
            super(servletRequest)
        }

        @Override
        String[] getParameterValues(String parameter) {
            String[] values = super.getParameterValues(parameter)

            if (!values) {
                return []
            }

            List<String> encoded = []
            values.each { value ->
                encoded.add(stripXSS(value))
            }

            return encoded as String[]
        }

        @Override
        String getParameter(String parameter) {
            String value = super.getParameter(parameter)
            return stripXSS(value)
        }

        @Override
        String getHeader(String name) {
            String value = super.getHeader(name)
            return stripXSS(value)
        }

        private String stripXSS(String value) {
            if (value) {
                // Avoid encoded attacks by parsing the value using ESAPI
                String encodedValue = ESAPI.encoder().canonicalize(value)

                // Avoid null characters
                encodedValue = encodedValue.replaceAll('', '')

                // Remove all sections that match a pattern
                for (Pattern pattern : PATTERNS) {
                    encodedValue = pattern.matcher(encodedValue).replaceAll('')
                }

                return encodedValue
            }
            return value
        }
    }
}
