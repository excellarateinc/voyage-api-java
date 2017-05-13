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

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Path

class GlobalExceptionHandlerSpec extends Specification {
    def 'handle() ConstraintViolationException returns an Bad Request error'() {
        given:
            GlobalExceptionHandler handler = new GlobalExceptionHandler()
            def emailConstraint = Mock(ConstraintViolation)
            def emailConstraintPath = Mock(Path)

            def notNullConstraint = Mock(ConstraintViolation)
            def notNullConstraintPath = Mock(Path)

        when:
            Set violations = new LinkedHashSet<? extends ConstraintViolation<?>>()
            violations.add(emailConstraint)
            violations.add(notNullConstraint)
            ConstraintViolationException exception = new ConstraintViolationException(violations)
            ResponseEntity<Iterable<ErrorResponse>> responseEntity = handler.handle(exception)

        then:
            emailConstraint.message >> 'Invalid email address'
            emailConstraint.propertyPath >> emailConstraintPath
            emailConstraintPath.toString() >> 'field.name'

            notNullConstraint.message >> 'Phone number required'
            notNullConstraint.propertyPath >> notNullConstraintPath
            notNullConstraintPath.toString() >> 'field.phone'

            responseEntity.statusCodeValue == 400
            responseEntity.body.size() == 2
            responseEntity.body*.error.contains('field.name.invalid_email_address')
            responseEntity.body*.errorDescription.contains('Invalid email address')
            responseEntity.body*.error.contains('field.phone.phone_number_required')
            responseEntity.body*.errorDescription.contains('Phone number required')
    }

    def 'handle() MethodArgumentNotValidException returns an Bad Request error'() {
        given:
            GlobalExceptionHandler handler = new GlobalExceptionHandler()
            def exception = Mock(MethodArgumentNotValidException)

            BindingResult bindingResult = Mock(BindingResult)
            FieldError usernameRequiredError = Mock(FieldError)
            FieldError firstNameRequiredError = Mock(FieldError)

        when:
            ResponseEntity<Iterable<ErrorResponse>> responseEntity = handler.handle(exception)

        then:
            exception.bindingResult >> bindingResult
            bindingResult.fieldErrors >> [usernameRequiredError, firstNameRequiredError]
            usernameRequiredError.code >> 'field.username'
            usernameRequiredError.defaultMessage >> 'Username is required'
            firstNameRequiredError.code >> 'field.firstName'
            firstNameRequiredError.defaultMessage >> 'First Name is required'

            responseEntity.statusCodeValue == 400
            responseEntity.body.size() == 2
            responseEntity.body[0].error == 'field.username'
            responseEntity.body[0].errorDescription == 'Username is required'
            responseEntity.body[1].error == 'field.firstName'
            responseEntity.body[1].errorDescription == 'First Name is required'
    }

    def 'handle() AppException returns an Bad Request error'() {
        given:
            GlobalExceptionHandler handler = new GlobalExceptionHandler()
            AppException exception = new AppException(HttpStatus.BAD_REQUEST, 'Default message')

        when:
            ResponseEntity<Iterable<ErrorResponse>> responseEntity = handler.handle(exception)

        then:
            responseEntity.statusCodeValue == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '400_bad_request'
            responseEntity.body[0].errorDescription == 'Default message'
    }
}
