/*
 * Copyright 2018 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
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
package voyage.security.user

import org.passay.RuleResultDetail
import org.springframework.http.HttpStatus
import spock.lang.Specification

class WeakPasswordExceptionSpec extends  Specification {
    def 'default exception creates a 400 Bad Request exception'() {
        when:
            WeakPasswordException ex = new WeakPasswordException()

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_weak_password'
    }

    def 'Override the exception message only affects the description'() {
        when:
            WeakPasswordException ex = new WeakPasswordException('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_weak_password'
            ex.message == 'TEST MESSAGE'
    }

    def 'Override the exception message with resultData only affects the description for one result'() {
        when:
            RuleResultDetail result1 = new RuleResultDetail('INSUFFICIENT_UPPERCASE', null)
            WeakPasswordException ex = new WeakPasswordException([result1])

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_weak_password'
            ex.message.contains('Minimum 1 uppercase character is required')
            !ex.message.contains('Minimum 1 special character is required')
    }

    def 'Override the exception message with resultData only affects the description for one multiple results'() {
        when:
            RuleResultDetail result1 = new RuleResultDetail('INSUFFICIENT_UPPERCASE', null)
            RuleResultDetail result2 = new RuleResultDetail('INSUFFICIENT_SPECIAL', null)
            RuleResultDetail result3 = new RuleResultDetail('INSUFFICIENT_LOWERCASE', null)
            RuleResultDetail result4 = new RuleResultDetail('INSUFFICIENT_DIGIT', null)
            RuleResultDetail result5 = new RuleResultDetail('TOO_SHORT', null)
            RuleResultDetail result6 = new RuleResultDetail('TOO_LONG', null)
            WeakPasswordException ex = new WeakPasswordException([result1, result2, result3, result4, result5, result6])

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_weak_password'
            ex.message.contains('Minimum 1 uppercase character is required')
            ex.message.contains('Minimum 1 special character is required')
            ex.message.contains('Minimum 1 lowercase character is required')
            ex.message.contains('Minimum 1 digit character is required')
            ex.message.contains('Minimum length of 8 characters')
            ex.message.contains('Maximum length of 100 characters')
    }
}
