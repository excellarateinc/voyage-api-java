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
package voyage.security.verify

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import voyage.security.user.User

class VerifyControllerSpec extends Specification {
    User user
    User modifiedUser
    VerifyService verifyService = Mock(VerifyService)
    VerifyController verifyController = new VerifyController(verifyService)

    def setup() {
        user = new User(id:1, firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password')
        modifiedUser = new User(id:1, firstName:'firstName', lastName:'LastName', username:'username', email:'test@test.com', password:'password')
    }

    def 'Test to validate sendVerificationCode method'() {
        when:
            ResponseEntity response = verifyController.sendVerificationCode()
        then:
            1 * verifyService.sendVerifyCodeToCurrentUser()
            HttpStatus.NO_CONTENT == response.statusCode

        when:
            verifyController.sendVerificationCode()
        then:
            1 * verifyService.sendVerifyCodeToCurrentUser() >> { throw new Exception() }
            thrown(Exception)
    }

    def 'Test to validate verify method'() {
        when:
            ResponseEntity response = verifyController.verify(new VerifyResource([code:'code']))
        then:
            1 * verifyService.verifyCurrentUser('code')
            HttpStatus.NO_CONTENT == response.statusCode

        when:
            verifyController.verify(new VerifyResource([code:'code']))
        then:
            1 * verifyService.verifyCurrentUser('code') >> { throw new Exception() }
            thrown(Exception)
    }
}
