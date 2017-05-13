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
package voyage.profile

import spock.lang.Specification
import voyage.core.mail.MailService
import voyage.security.user.PhoneType
import voyage.security.user.User
import voyage.security.user.UserPhone
import voyage.security.user.UserService

class ProfileServiceSpec extends Specification {
    User user
    UserService userService = Mock()
    MailService mailService = Mock()
    ProfileService profileService = new ProfileService(userService, mailService)

    def setup() {
        profileService.appName = 'Voyage'
    }

    def 'save - applies the values and calls the userService'() {
        given:
            User userIn = new User(
                    firstName:'John',
                    lastName:'Doe',
                    email:'john@doe.com',
                    username:'jdoe',
                    password:'my-secure-password',
            )
            userIn.phones = [
                    new UserPhone(phoneNumber:'111-111-1111', phoneType:PhoneType.MOBILE, user:userIn),
                    new UserPhone(phoneNumber:'222-222-2222', phoneType:PhoneType.MOBILE, user:userIn),
            ]

        when:
            User savedUser = profileService.save(userIn)

        then:
            1 * userService.saveDetached(*_) >> { args ->
                return args[0] // return the given user back
            }
            1 * mailService.send(*_) >> { args ->
                assert args[0].to == 'john@doe.com'
                assert args[0].subject == 'Welcome to Voyage'
                assert args[0].template == 'welcome.ftl'
            }

            savedUser.firstName == 'John'
            savedUser.lastName == 'Doe'
            savedUser.email == 'john@doe.com'
            savedUser.username == 'jdoe'
            savedUser.password == 'my-secure-password'
            savedUser.isEnabled
            savedUser.isVerifyRequired
            savedUser.phones.size() == 2
            savedUser.phones[0].phoneNumber == '111-111-1111'
            savedUser.phones[0].phoneType == PhoneType.MOBILE
            savedUser.phones[1].phoneNumber == '222-222-2222'
            savedUser.phones[1].phoneType == PhoneType.MOBILE
    }

    def 'save - Welcome email is not sent if the user does not provide an email address'() {
        given:
            User userIn = new User(
                    firstName:'John',
                    lastName:'Doe',
                    username:'jdoe',
                    password:'my-secure-password',
            )

        when:
            User savedUser = profileService.save(userIn)

        then:
            1 * userService.saveDetached(*_) >> { args ->
                return args[0] // return the given user back
            }
            0 * mailService.send(_)

            savedUser.firstName == 'John'
            savedUser.lastName == 'Doe'
            !savedUser.email
            savedUser.username == 'jdoe'
            savedUser.password == 'my-secure-password'
            savedUser.isEnabled
            savedUser.isVerifyRequired
            !savedUser.phones
    }
}
