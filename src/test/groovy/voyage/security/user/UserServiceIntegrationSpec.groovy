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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import voyage.security.crypto.CryptoService

@SpringBootTest
class UserServiceIntegrationSpec extends Specification {

    @Autowired
    private UserService userService

    @Autowired CryptoService cryptoService
    User user

    def setup() {
        user = new User(
                username:'username', firstName:'LSS', lastName:'India', password:'Test@1234', isVerifyRequired:false,
                isEnabled:false, isAccountExpired:false, isAccountLocked:false, isCredentialsExpired:false,
        )
        user.phones = [new UserPhone(phoneNumber:'+16518886020', phoneType:PhoneType.MOBILE)]
    }

    def 'save - update User details with Valid Password'() {
        given:
        SecurityContext securityContext = Mock(SecurityContext)
        SecurityContextHolder.context = securityContext
        Authentication authentication = Mock(Authentication)
        securityContext.authentication >> authentication
        authentication.isAuthenticated() >> true
        authentication.principal >> user

        when:
        User savedUser = userService.saveDetached(user)

        then:
            savedUser.username == 'username'
            cryptoService.hashMatches('Test@1234', savedUser.password)
    }

    def 'save - update user with weak password'() {
        when:
            user.username = 'username_1'
            user.password = password
            userService.saveDetached(user)

        then:
            thrown(WeakPasswordException)

        where:
            sno     |   password
            1       |   'test'
            2       |   'test@1234'
            3       |   'test1234'
            4       |   'password'
            5       |   'Password@'
    }
}
