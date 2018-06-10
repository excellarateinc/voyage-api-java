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

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import voyage.security.AuthenticatedIntegrationTest
import voyage.security.crypto.CryptoService

import javax.mail.internet.MimeMessage

class PasswordResetControllerIntegrationSpec extends AuthenticatedIntegrationTest {
    private GreenMail greenMailSMTP

    @Autowired
    UserService userService

    @Autowired
    CryptoService cryptoService

    @Autowired
    PasswordResetService passwordResetService

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

    def '/api/v1/password/forgot POST - Anonymous access not allowed (must have valid client)'() {
        given:
            PasswordResetRequest resetRequest = new PasswordResetRequest(
                email:'client-standard@user.com',
                redirectUri:'http://localhost:3001/password/reset'
            )
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<PasswordResetRequest> httpEntity = new HttpEntity<PasswordResetRequest>(resetRequest, headers)

        when:
            ResponseEntity<Iterable> responseEntity = POST('/api/v1/password/forgot', httpEntity, Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/password/forgot POST - Request password reset on Standard User succeeds with threading enabled'() {
        given:
            passwordResetService.isTestingEnabled = true
            PasswordResetRequest resetRequest = new PasswordResetRequest(
                email:'client-standard@user.com',
                redirectUri:'http://localhost:3001/password/reset'
            )
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<PasswordResetRequest> httpEntity = new HttpEntity<PasswordResetRequest>(resetRequest, headers)

        when:
            ResponseEntity<String> responseEntity = POST('/api/v1/password/forgot', httpEntity, String, standardClient)

        then:
            responseEntity.statusCode.value() == 200
            !responseEntity.body

            MimeMessage[] emails = greenMailSMTP.receivedMessages
            emails.size() == 1
            emails[0].allRecipients.size() == 1
    }

    def '/api/v1/password/forgot POST - Request password reset on Standard User with threading disabled'() {
        given:
            passwordResetService.isTestingEnabled = true
            PasswordResetRequest resetRequest = new PasswordResetRequest(
                email:'client-standard@user.com',
                redirectUri:'http://localhost:3001/password/reset'
            )
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<PasswordResetRequest> httpEntity = new HttpEntity<PasswordResetRequest>(resetRequest, headers)

        when:
            ResponseEntity<String> responseEntity = POST('/api/v1/password/forgot', httpEntity, String, standardClient)

        then:
            responseEntity.statusCode.value() == 200
            !responseEntity.body

            User user = userService.findByEmail('client-standard@user.com')
            user.passwordResetToken != null
            user.passwordResetDate != null

            MimeMessage[] emails = greenMailSMTP.receivedMessages
            emails.size() == 1
            emails[0].allRecipients.size() == 1
    }

    def '/api/v1/password/reset POST - Save reset on Standard User'() {
        // Invoke /forgot
        given:
            passwordResetService.isTestingEnabled = true

        when:
            PasswordResetRequest forgotRequest = new PasswordResetRequest(
                email:'client-standard@user.com',
                redirectUri:'http://localhost:3001/password/reset'
            )
            HttpHeaders forgotRequestHeaders = new HttpHeaders()
            forgotRequestHeaders.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<PasswordResetRequest> forgotRequestHttpEntity = new HttpEntity<PasswordResetRequest>(forgotRequest, forgotRequestHeaders)

            ResponseEntity<String> forgotRequestResponse = POST('/api/v1/password/forgot', forgotRequestHttpEntity, String, standardClient)

        then:
            forgotRequestResponse.statusCode.value() == 200
            !forgotRequestResponse.body

            User user = userService.findByEmail('client-standard@user.com')
            user.passwordResetToken != null
            user.passwordResetDate != null

            MimeMessage[] emails = greenMailSMTP.receivedMessages
            emails.size() == 1
            emails[0].allRecipients.size() == 1

            // Fetch the token from the email body
            String emailBody = GreenMailUtil.getBody(emails[0])
            def matcher = (emailBody =~ /(?s)&token=3D(.*?)"/)
            String tokenRaw = matcher[0][1]
            String token = tokenRaw.replaceAll('=\r\n', '')

        when:
            PasswordResetRequest resetRequest = new PasswordResetRequest(email:'client-standard@user.com', password:'Test555$$', token:token)
            HttpHeaders resetRequestHeaders = new HttpHeaders()
            resetRequestHeaders.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<PasswordResetRequest> resetRequestHttpEntity = new HttpEntity<PasswordResetRequest>(resetRequest, resetRequestHeaders)

            ResponseEntity<String> resetRequestResponse = POST('/api/v1/password/reset', resetRequestHttpEntity, String, standardClient)

        then:
            resetRequestResponse.statusCode.value() == 200
            !resetRequestResponse.body

            User updatedUser = userService.findByEmail('client-standard@user.com')
            !updatedUser.passwordResetToken
            !updatedUser.passwordResetDate
            cryptoService.hashMatches('Test555$$', updatedUser.password)
    }

    def '/api/v1/password/reset POST - Fails due to token expired'() {
        given:
            User user = userService.findByEmail('client-standard@user.com')
            user.passwordResetToken = cryptoService.hashEncode('TEST-TOKEN')
            user.passwordResetDate = new Date() - 1
            userService.save(user)

        when:
            PasswordResetRequest resetRequest = new PasswordResetRequest(email:'client-standard@user.com', password:'Test555$$', token:'TEST-TOKEN')
            HttpHeaders resetRequestHeaders = new HttpHeaders()
            resetRequestHeaders.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<PasswordResetRequest> resetRequestHttpEntity = new HttpEntity<PasswordResetRequest>(resetRequest, resetRequestHeaders)

            ResponseEntity<Iterable> resetRequestResponse = POST('/api/v1/password/reset', resetRequestHttpEntity, Iterable, standardClient)

        then:
            resetRequestResponse.statusCode.value() == 400
            resetRequestResponse.body.size() == 1
            resetRequestResponse.body[0].error == '400_password_reset_token_expired'
            resetRequestResponse.body[0].errorDescription == 'Password reset token has expired'
    }

    def '/api/v1/password/reset POST - Fails with wrong token'() {
        when:
            PasswordResetRequest resetRequest = new PasswordResetRequest(email:'client-standard@user.com', password:'Test555$$', token:'TEST-TOKEN')
            HttpHeaders resetRequestHeaders = new HttpHeaders()
            resetRequestHeaders.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<PasswordResetRequest> resetRequestHttpEntity = new HttpEntity<PasswordResetRequest>(resetRequest, resetRequestHeaders)

            ResponseEntity<Iterable> resetRequestResponse = POST('/api/v1/password/reset', resetRequestHttpEntity, Iterable, standardClient)

        then:
            resetRequestResponse.statusCode.value() == 400
            resetRequestResponse.body.size() == 1
            resetRequestResponse.body[0].error == '400_password_reset_token_expired'
            resetRequestResponse.body[0].errorDescription == 'Password reset token has expired'
    }

    def '/api/v1/password/reset POST - Fails with invalid e-mail'() {
        when:
            PasswordResetRequest resetRequest = new PasswordResetRequest(email:'not-a-valid-email@user.com', password:'Test555$$', token:'TEST-TOKEN')
            HttpHeaders resetRequestHeaders = new HttpHeaders()
            resetRequestHeaders.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<PasswordResetRequest> resetRequestHttpEntity = new HttpEntity<PasswordResetRequest>(resetRequest, resetRequestHeaders)

            ResponseEntity<Iterable> resetRequestResponse = POST('/api/v1/password/reset', resetRequestHttpEntity, Iterable, standardClient)

        then:
            resetRequestResponse.statusCode.value() == 400
            resetRequestResponse.body.size() == 1
            resetRequestResponse.body[0].error == '400_password_reset_token_expired'
            resetRequestResponse.body[0].errorDescription == 'Password reset token has expired'
    }
}
