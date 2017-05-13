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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.ErrorAttributes
import org.springframework.boot.autoconfigure.web.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.ServletRequestAttributes

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class GlobalExceptionController implements ErrorController {
    private final ErrorAttributes errorAttributes
    private final GlobalExceptionHandler globalExceptionHandler

    String errorPath = '/error' // Overrides ErrorController.getErrorPath()

    @Autowired
    GlobalExceptionController(ErrorAttributes errorAttributes, GlobalExceptionHandler globalExceptionHandler) {
        this.errorAttributes = errorAttributes
        this.globalExceptionHandler = globalExceptionHandler
    }

    @RequestMapping(value = '/error')
    ResponseEntity<Iterable<ErrorResponse>> handleError(HttpServletRequest request, HttpServletResponse response) {
        // Handle AppExceptions by the definition embedded in the exception
        Exception exception = (Exception)request.getAttribute('javax.servlet.error.exception')
        if (exception instanceof AppException) {
            return globalExceptionHandler.handle((AppException)exception)
        }

        // Handle unknown exceptions based on the error details given
        Map errorMap = getErrorAttributes(request, false)
        String errorCode = ErrorUtils.getErrorCode((int)errorMap.status)
        String errorMessage = "${errorMap.status} ${errorMap.error}. ${errorMap.message}"
        ErrorResponse errorResponse = new ErrorResponse(
            error:errorCode,
            errorDescription:errorMessage,
        )
        return new ResponseEntity([errorResponse], HttpStatus.valueOf(response.status))
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace = false) {
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request)
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace)
    }
}
