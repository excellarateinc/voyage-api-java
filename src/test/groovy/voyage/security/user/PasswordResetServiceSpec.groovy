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
package voyage.security.user

import groovy.time.TimeCategory
import spock.lang.Specification
import voyage.core.mail.MailService
import voyage.security.client.Client
import voyage.security.client.ClientService
import voyage.security.crypto.CryptoService

class PasswordResetServiceSpec extends Specification {
    PasswordResetService passwordResetService
    ClientService clientService
    UserService userService
    MailService mailService
    CryptoService cryptoService

    def setup() {
        clientService = Mock()
        userService = Mock()
        mailService = Mock()
        cryptoService = Mock()
        passwordResetService = new PasswordResetService(clientService, userService, mailService, cryptoService)
        passwordResetService.isTestingEnabled = true
    }

    def 'sendResetMessage throws exception if no redirect URI is found'() {
        given:
            String redirectUri = 'test2'
        when:
            passwordResetService.sendApiResetMessage('test', redirectUri)
        then:
            1 * clientService.getPasswordResetRedirectUri(_, redirectUri) >> null
            thrown(PasswordResetNotConfiguredException)
    }

    def 'sendResetMessage does nothing when no user found'() {
        given:
            String email = 'test'
            Client client = new Client()
        when:
            passwordResetService.sendResetMessageThreadTask(client, email, 'testUri')
        then:
            1 * userService.findByEmail(email) >> null
            0 * userService.saveDetached(_)
            0 * mailService.send(_)
    }

    def 'sendResetMessage sends a reset email to the matching user'() {
        given:
            String email = 'test'
            User user = new User()
            Client client = new Client()
        when:
            passwordResetService.sendResetMessageThreadTask(client, email, 'testUri')
        then:
            1 * userService.findByEmail(email) >> user
            1 * cryptoService.hashEncode(_) >> 'token'
            1 * userService.save(user)
            1 * clientService.getPasswordResetRedirectUri(client, _) >> 'redirect'
            1 * mailService.send(_)
    }

    def 'getPasswordResetLink applies email and token with no existing URL params'() {
        given:
            Client client = new Client()
        when:
            String link = passwordResetService.getPasswordResetLink(client, '/test', 'test-token', 'test-email')
        then:
            1 * clientService.getPasswordResetRedirectUri(_, _) >> '/test'
            link == '/test?email=test-email&token=test-token'
    }

    def 'getPasswordResetLink applies email and token with existing URL params'() {
        given:
            Client client = new Client()
        when:
           String link = passwordResetService.getPasswordResetLink(client, '/test?test=test', 'test-token', 'test-email')
        then:
            1 * clientService.getPasswordResetRedirectUri(_, _) >> '/test?test=test'
            link == '/test?test=test&email=test-email&token=test-token'
    }

    def 'reset cannot find user throws exception'() {
        when:
            passwordResetService.reset('email', 'token', 'new-password')
        then:
            1 * userService.findByEmail('email') >> null
            thrown(PasswordResetTokenExpiredException)
    }

    def 'reset with user that has no stored token throws an exception'() {
        when:
            passwordResetService.reset('email', 'token', 'new-password')
        then:
            1 * userService.findByEmail('email') >> new User()
            thrown(PasswordResetTokenExpiredException)
    }

    def 'reset with user that has no stored password reset date throws an exception'() {
        when:
           passwordResetService.reset('email', 'token', 'new-password')
        then:
            1 * userService.findByEmail('email') >> new User(passwordResetToken:'test')
            thrown(PasswordResetTokenExpiredException)
    }

    def 'reset with expired token throws exception'() {
        given:
            passwordResetService.passwordResetTokenExpiresMinutes = 90
            Date resetDate = new Date()
            use(TimeCategory) {
                resetDate = resetDate - 91.minutes
            }
        when:
           passwordResetService.reset('email', 'token', 'new-password')
        then:
            1 * userService.findByEmail('email') >> new User(passwordResetToken:'test', passwordResetDate:resetDate)
            thrown(PasswordResetTokenExpiredException)
    }

    def 'reset with valid token updates the password'() {
        given:
            passwordResetService.passwordResetTokenExpiresMinutes = 90
            Date resetDate = new Date()
            use(TimeCategory) {
                resetDate = resetDate - 89.minutes
            }
            User user = new User(passwordResetToken:'test', passwordResetDate:resetDate)
        when:
           passwordResetService.reset('email', 'token', 'new-password')
        then:
            1 * userService.findByEmail('email') >> user
            1 * cryptoService.hashMatches(_, _) >> true
            1 * userService.save(user)
    }
}
