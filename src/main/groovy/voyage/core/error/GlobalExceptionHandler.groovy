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
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

import javax.validation.ConstraintViolationException

@ControllerAdvice
@Order(1)
class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler)

    @ExceptionHandler
    ResponseEntity<Iterable<ErrorResponse>> handle(ConstraintViolationException e) {
        List errorResponses = []
        e.constraintViolations.each { violation ->
            ErrorResponse errorResponse = new ErrorResponse(
                    error:ErrorUtils.formatErrorCode(violation.propertyPath.toString() + '.' + violation.message),
                    errorDescription:violation.message,
            )
            errorResponses.add(errorResponse)
        }
        return new ResponseEntity(errorResponses, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    ResponseEntity<Iterable<ErrorResponse>> handle(MethodArgumentNotValidException e) {
        List errorResponses = []
        e.bindingResult.fieldErrors.each { fieldError ->
            ErrorResponse errorResponse = new ErrorResponse(
                    error:fieldError.code,
                    errorDescription:fieldError.defaultMessage,
            )
            errorResponses.add(errorResponse)
        }
        return new ResponseEntity(errorResponses, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    ResponseEntity<Iterable<ErrorResponse>> handle(AppException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                error:e.errorCode,
                errorDescription:e.message,
        )
        return new ResponseEntity([errorResponse], e.httpStatus)
    }

    @ExceptionHandler
    ResponseEntity<Iterable<ErrorResponse>> handle(HttpMediaTypeNotSupportedException ex) {
        if (LOG.debugEnabled) {
            LOG.debug('Http Media Type Not Supported', ex)
        }
        ErrorResponse errorResponse = new ErrorResponse(
                error:'400_media_type_not_supported',
                errorDescription:'Invalid media type for the HTTP request. Please use application/json.',
        )
        return new ResponseEntity([errorResponse], HttpStatus.BAD_REQUEST)
    }
}
