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
import spock.lang.Specification

class WebResponseExceptionTranslatorSpec extends Specification {

    def 'translate RedirectMismatchException as a ResponseEntity'() {
        given:
            RedirectMismatchException exception = Mock(RedirectMismatchException)
            WebResponseExceptionTranslator translator = new WebResponseExceptionTranslator()

        when:
            ResponseEntity responseEntity = translator.translate(exception)

        then:
            responseEntity.statusCode == HttpStatus.BAD_REQUEST
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == '*'
            responseEntity.body.httpStatus == HttpStatus.BAD_REQUEST
            responseEntity.body.message == 'The client redirect URL was not valid. To resolve this issue please use a valid redirect url.'
    }

    def 'translate an OAuth2Exception as a ResponseEntity'() {
        given:
            OAuth2Exception exception = new OAuth2Exception('test msg', new RuntimeException())
            WebResponseExceptionTranslator translator = new WebResponseExceptionTranslator()

        when:
            ResponseEntity responseEntity = translator.translate(exception)

        then:
            responseEntity.statusCode == HttpStatus.BAD_REQUEST
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == '*'
            responseEntity.body.httpStatus == HttpStatus.BAD_REQUEST
            responseEntity.body.message == 'test msg'
    }

    def 'translate an AuthenticationException as a ResponseEntity'() {
        given:
            AuthenticationException exception = Mock(AuthenticationException)
            WebResponseExceptionTranslator translator = new WebResponseExceptionTranslator()

        when:
            ResponseEntity responseEntity = translator.translate(exception)

        then:
            1 * exception.message >> 'test msg'
            responseEntity.statusCode == HttpStatus.UNAUTHORIZED
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == '*'
            responseEntity.body.httpStatus == HttpStatus.UNAUTHORIZED
            responseEntity.body.message == 'test msg'
    }

    def 'translate an unknown Exception type as a ResponseEntity'() {
        given:
            RuntimeException exception = new RuntimeException('test msg')
            WebResponseExceptionTranslator translator = new WebResponseExceptionTranslator()

        when:
            ResponseEntity responseEntity = translator.translate(exception)

        then:
            responseEntity.statusCode == HttpStatus.BAD_REQUEST
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == '*'
            responseEntity.body.httpStatus == HttpStatus.BAD_REQUEST
            responseEntity.body.message == 'test msg'
    }
}
