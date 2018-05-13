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

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import voyage.security.AuthenticatedIntegrationTest
import voyage.security.crypto.CryptoService
import voyage.security.user.PhoneType
import voyage.security.user.User
import voyage.security.user.UserPhone
import voyage.security.user.UserService

import javax.mail.internet.MimeMessage

class ProfileControllerIntegrationSpec extends AuthenticatedIntegrationTest {
    private GreenMail greenMailSMTP

    @Autowired
    private UserService userService

    @Autowired
    private CryptoService cryptoService

    @Value('${spring.mail.host}')
    private String mailServerHost

    @Value('${spring.mail.port}')
    private int mailServerPort

    def setup() {
        ServerSetup setup = new ServerSetup(mailServerPort, mailServerHost, ServerSetup.PROTOCOL_SMTP)
        greenMailSMTP = new GreenMail(setup)
        greenMailSMTP.start()
    }

    def cleanup() {
        greenMailSMTP.stop()
    }

    def '/api/v1/profiles/register POST - Profile create'() {
        given:
            User user = new User(firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password')
            user.phones = [new UserPhone(phoneNumber:'+16124590457', phoneType:PhoneType.MOBILE)]
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity responseEntity = POST('/api/v1/profiles/register', httpEntity, String, standardClient)
            User savedUser = userService.findByUsername(user.username)

        then:
            responseEntity.statusCode.value() == 201
            savedUser.firstName == 'Test1'
            savedUser.lastName == 'User'
            savedUser.username == 'username'
            savedUser.email == 'test@test.com'
            cryptoService.hashMatches('password', savedUser.password)
            savedUser.phones.size() == 1
            savedUser.isVerifyRequired
            savedUser.isEnabled
            savedUser.phones[0].phoneType == PhoneType.MOBILE
            savedUser.phones[0].phoneNumber == '+16124590457'

            MimeMessage[] emails = greenMailSMTP.receivedMessages
            emails.size() == 1
            emails[0].allRecipients.size() == 1
    }

    def '/api/v1/profiles/register POST - Profile create fails with error due to username already in use'() {
        given:
            User user = new User(firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password')
            user.phones = [new UserPhone(phoneNumber:'+1-111-111-1111', phoneType:PhoneType.MOBILE)]
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<List> responseEntity = POST('/api/v1/profiles/register', httpEntity, List, standardClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body[0].error == '400_username_already_in_use'
            responseEntity.body[0].errorDescription == 'Username already in use by another user. Please choose a different username.'
    }

    def '/api/v1/profiles/register POST - Profile create fails with error due to missing required values'() {
        given:
            User user = new User()
            user.phones = [new UserPhone(phoneNumber:'+1-800-888-8888', phoneType:PhoneType.MOBILE)]
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<List> responseEntity = POST('/api/v1/profiles/register', httpEntity, List, standardClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body.size() == 4
            responseEntity.body.find { it.error == 'password.may_not_be_empty' && it.errorDescription == 'may not be empty' }
            responseEntity.body.find { it.error == 'firstname.may_not_be_empty' && it.errorDescription == 'may not be empty' }
            responseEntity.body.find { it.error == 'username.may_not_be_empty' && it.errorDescription == 'may not be empty' }
            responseEntity.body.find { it.error == 'lastname.may_not_be_empty' && it.errorDescription == 'may not be empty' }
    }

    def '/api/v1/profiles/register POST - Profile create fails with error due to email format invalid'() {
        given:
            User user = new User(firstName:'Test1', lastName:'User', username:'username44', email:'test@', password:'password')
            user.phones = [new UserPhone(phoneNumber:'+1-800-888-8888', phoneType:PhoneType.MOBILE)]
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<List> responseEntity = POST('/api/v1/profiles/register', httpEntity, List, standardClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body[0].error == 'email.not_a_well-formed_email_address'
            responseEntity.body[0].errorDescription == 'not a well-formed email address'
    }

    def '/api/v1/profiles/register POST - Profile create fails with error due to missing mobile phone'() {
        given:
            User user = new User(firstName:'Test1', lastName:'User', username:'username22', email:'test@test.com', password:'password')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<List> responseEntity = POST('/api/v1/profiles/register', httpEntity, List, standardClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body[0].error == '400_mobile_phone_required'
            responseEntity.body[0].errorDescription == 'At least one mobile phone is required for a new profile'
    }

    def '/api/v1/profiles/register POST - Profile create fails with error due to > 5 phones'() {
        given:
            User user = new User(firstName:'Test1', lastName:'User', username:'username21', email:'test@test.com', password:'password')
            user.phones = [
                new UserPhone(phoneNumber:'+1205-111-1111', phoneType:PhoneType.MOBILE),
                new UserPhone(phoneNumber:'+1222-222-2222', phoneType:PhoneType.MOBILE),
                new UserPhone(phoneNumber:'+1333-333-3333', phoneType:PhoneType.MOBILE),
                new UserPhone(phoneNumber:'+1444-444-4444', phoneType:PhoneType.MOBILE),
                new UserPhone(phoneNumber:'+1555-555-5555', phoneType:PhoneType.MOBILE),
                new UserPhone(phoneNumber:'+1666-666-6666', phoneType:PhoneType.MOBILE),
            ]

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<List> responseEntity = POST('/api/v1/profiles/register', httpEntity, List, standardClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body[0].error == '400_too_many_phones'
            responseEntity.body[0].errorDescription == 'Too many phones have been added to the profile. Maximum of 5.'
    }

    def '/api/v1/profiles/register POST - Profile create succeeds with mixed case phone type'() {
        given:
            String body = '{"firstName":"Tom", "lastName":"Jones", "username":"tjones", "email":"test@test.com", "password":"password", ' +
                '"phones":[{"phoneType":"MobilE", "phoneNumber":"+16124590457"}]}'

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers)

        when:
            ResponseEntity<List> responseEntity = POST('/api/v1/profiles/register', httpEntity, List, standardClient)

        then:
            responseEntity.statusCode.value() == 201
    }

    def '/api/v1/profiles/register POST - Profile create succeeds with an invalid phone type'() {
        given:
           String body = '{"firstName":"Tom", "lastName":"Jones", "username":"tjones123", "email":"tjones123@test.com", "password":"password", ' +
                '"phones":[{"phoneType":"BLAH", "phoneNumber":"+16124590457"}, {"phoneType":"mobile", "phoneNumber":"+16514590457"}]}'

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers)

        when:
            ResponseEntity<List> responseEntity = POST('/api/v1/profiles/register', httpEntity, List, standardClient)

        then:
            responseEntity.statusCode.value() == 201
    }

    def '/api/v1/profiles/me GET - Returns the user profile of the current Super user'() {
        when:
           ResponseEntity<User> responseEntity = GET('/api/v1/profiles/me', User, superClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.firstName == 'Super Client'
            responseEntity.body.lastName == 'User Client'
            responseEntity.body.username == 'client-super'
    }

    def '/api/v1/profiles/me GET - Returns the user profile of the current Standard user'() {
        when:
            ResponseEntity<User> responseEntity = GET('/api/v1/profiles/me', User, standardClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.firstName == 'Standard'
            responseEntity.body.lastName == 'User'
            responseEntity.body.username == 'client-standard'
    }

    def '/api/v1/profiles/me GET - Access denied for Anonymous'() {
        when:
           ResponseEntity<Iterable> responseEntity = GET('/api/v1/profiles/me', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }
}
