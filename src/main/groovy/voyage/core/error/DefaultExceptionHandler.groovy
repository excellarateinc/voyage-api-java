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
package voyage.core.error

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class DefaultExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionHandler)

    @ExceptionHandler(value = Exception)
    ResponseEntity<Iterable<ErrorResponse>> handle(Exception e) {
        LOG.error('Unexpected error occurred', e)
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
        ErrorResponse errorResponse = new ErrorResponse(
                error:ErrorUtils.getErrorCode(httpStatus.value()),
                errorDescription:'Unexpected error occurred. Contact technical support for further assistance should this error continue.',
        )
        return new ResponseEntity([errorResponse], httpStatus)
    }
}
