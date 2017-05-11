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
package voyage.security.error

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception
import org.springframework.security.oauth2.common.exceptions.RedirectMismatchException
import org.springframework.stereotype.Component

@Component
class WebResponseExceptionTranslator implements org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator {
    private static final String CORS_ACCESS_CONTROL_ALLOW_ORIGIN = 'Access-Control-Allow-Origin'
    private static final String CORS_ACCESS_WILDCARD = '*'

    @Override
    ResponseEntity<AppOAuth2Exception> translate(Exception e) throws Exception {
        if (e instanceof RedirectMismatchException) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .header(CORS_ACCESS_CONTROL_ALLOW_ORIGIN, CORS_ACCESS_WILDCARD)
                    .body(new OAuth2ClientRedirectException())

        } else if (e instanceof OAuth2Exception) {
            OAuth2Exception oAuth2Exception = (OAuth2Exception) e
            return ResponseEntity
                    .status(oAuth2Exception.httpErrorCode)
                    .header(CORS_ACCESS_CONTROL_ALLOW_ORIGIN, CORS_ACCESS_WILDCARD)
                    .body(new AppOAuth2Exception(HttpStatus.valueOf(oAuth2Exception.httpErrorCode), oAuth2Exception.message))

        } else if (e instanceof AuthenticationException) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .header(CORS_ACCESS_CONTROL_ALLOW_ORIGIN, CORS_ACCESS_WILDCARD)
                    .body(new AppOAuth2Exception(HttpStatus.UNAUTHORIZED, e.message))
        }

        return ResponseEntity
                .badRequest()
                .header(CORS_ACCESS_CONTROL_ALLOW_ORIGIN, CORS_ACCESS_WILDCARD)
                .body(new AppOAuth2Exception(HttpStatus.BAD_REQUEST, e.message))
    }
}
