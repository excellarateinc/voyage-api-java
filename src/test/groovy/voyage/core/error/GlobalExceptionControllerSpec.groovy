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

import org.springframework.boot.autoconfigure.web.ErrorAttributes
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.RequestAttributes
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class GlobalExceptionControllerSpec extends Specification {
    def 'handleError() processes a HttpServlet exception'() {
        given:
            ErrorAttributes errorAttributes = Mock(ErrorAttributes)
            GlobalExceptionHandler handler = new GlobalExceptionHandler()

            Map errorMap = [status:400, error:'test error', message:'test message']
            GlobalExceptionController controller = new GlobalExceptionController(errorAttributes, handler)

            def httpServletRequest = Mock(HttpServletRequest)
            def httpServletResponse = Mock(HttpServletResponse)

        when:
            ResponseEntity<Iterable<ErrorResponse>> responseEntity = controller.handleError(httpServletRequest, httpServletResponse)

        then:
            errorAttributes.getErrorAttributes(_ as RequestAttributes, false) >> errorMap
            httpServletResponse.status >> HttpStatus.BAD_REQUEST.value()

            responseEntity.statusCodeValue == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '400_bad_request'
            responseEntity.body[0].errorDescription == '400 test error. test message'
    }

    def 'handleError() routes to handle(AppException) if an AppException is found in the request attributes'() {
        given:
            GlobalExceptionHandler handler = new GlobalExceptionHandler()
            GlobalExceptionController controller = new GlobalExceptionController(null, handler)

            def httpServletRequest = Mock(HttpServletRequest)
            def httpServletResponse = Mock(HttpServletResponse)

        when:
           ResponseEntity<Iterable<ErrorResponse>> responseEntity = controller.handleError(httpServletRequest, httpServletResponse)

        then:
            httpServletRequest.getAttribute('javax.servlet.error.exception') >> new AppException(HttpStatus.BAD_REQUEST, 'Test message')

            responseEntity.statusCodeValue == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '400_bad_request'
            responseEntity.body[0].errorDescription == 'Test message'
    }
}
