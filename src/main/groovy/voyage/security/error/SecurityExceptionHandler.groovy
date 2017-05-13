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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import voyage.core.error.ErrorResponse
import voyage.core.error.ErrorUtils

@ControllerAdvice
@Order(2)
class SecurityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityExceptionHandler)

    @ExceptionHandler
    ResponseEntity<Iterable<ErrorResponse>> handle(AccessDeniedException ex) {
        if (LOG.debugEnabled) {
            LOG.debug('Access denied', ex)
        }
        ErrorResponse errorResponse = new ErrorResponse(
            error:ErrorUtils.getErrorCode(HttpStatus.UNAUTHORIZED.value()),
            errorDescription:'401 Unauthorized. Access Denied',
        )
        return new ResponseEntity([errorResponse], HttpStatus.UNAUTHORIZED)
    }
}
