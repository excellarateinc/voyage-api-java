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
package voyage.profile

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import voyage.security.user.User
import voyage.security.user.UserService
import voyage.security.verify.VerifyService

class ProfileControllerSpec extends Specification {
    User user
    User modifiedUser
    ProfileService profileService = Mock(ProfileService)
    VerifyService userVerifyService = Mock(VerifyService)
    UserService userService = Mock(UserService)
    ProfileController profileController = new ProfileController(profileService, userVerifyService, userService)

    def setup() {
        user = new User(id:1, firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password')
        modifiedUser = new User(id:1, firstName:'firstName', lastName:'LastName', username:'username', email:'test@test.com', password:'password')
    }

    def '/profiles/register saves successfully'() {
        when:
            ResponseEntity<User> response = profileController.register(user)
        then:
            1 * profileService.register(user) >> modifiedUser
            response != null
            HttpStatus.CREATED == response.statusCode
            '/v1/profiles/me' == response.headers.location[0]
    }

    def '/profiles/me returns the current user record successfully'() {
        when:
           ResponseEntity<User> response = profileController.myProfile()
        then:
            1 * userService.currentUser >> modifiedUser
            response != null
            HttpStatus.OK == response.statusCode
            'username' == response.body.username
    }
}
