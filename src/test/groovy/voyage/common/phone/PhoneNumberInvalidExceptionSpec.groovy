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
package voyage.common.phone

import org.springframework.http.HttpStatus
import spock.lang.Specification

class PhoneNumberInvalidExceptionSpec extends Specification {

    def 'default exception creates a 400 Bad Request exception'() {
        when:
            PhoneNumberInvalidException ex = new PhoneNumberInvalidException()

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_phone_invalid'
            ex.message == 'The phone number provided is not recognized.'
    }

    def 'Override the exception message only affects the description'() {
        when:
            PhoneNumberInvalidException ex = new PhoneNumberInvalidException('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_phone_invalid'
            ex.message == 'TEST MESSAGE'
    }

    def 'Override the exception message and extend the code'() {
        when:
            PhoneNumberInvalidException ex = new PhoneNumberInvalidException('TEST MESSAGE', 'EXT')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_phone_invalid_ext'
            ex.message == 'TEST MESSAGE'
    }
}
