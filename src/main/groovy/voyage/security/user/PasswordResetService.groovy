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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import voyage.core.mail.MailMessage
import voyage.core.mail.MailService
import voyage.security.client.ClientService
import voyage.security.crypto.CryptoService

import javax.validation.constraints.NotNull

@Transactional
@Service
@Validated
class PasswordResetService {
    private final ClientService clientService
    private final UserService userService
    private final MailService mailService
    private final CryptoService cryptoService

    @Value('${app.name}')
    private String appName

    @Value('${security.password-verification.password-reset-token-expires-minutes}')
    private int passwordResetTokenExpiresMinutes

    @Autowired
    PasswordResetService(ClientService clientService, UserService userService, MailService mailService, CryptoService cryptoService) {
        this.clientService = clientService
        this.userService = userService
        this.mailService = mailService
        this.cryptoService = cryptoService
    }

    void sendResetMessage(@NotNull String email, String passwordRedirectUri) {
        // Validate the client config outside of the thread so that an exception is returned to the consumer
        validateClientConfig(passwordRedirectUri)

        // Spawn a new thread so that the request returns the same response time for a valid or invalid email. An attacker
        // could determine a valid email address from an invalid email by the duration of the response if the task is not
        // completed within a separate thread allowing this method to return immediately.
        Thread.start {
            sendResetMessageThreadTask(email, passwordRedirectUri)
        }
    }

    private void sendResetMessageThreadTask(String email, String passwordRedirectUri) {
        User user = userService.findByEmail(email)
        if (user) {
            String passwordResetToken = cryptoService.secureRandomToken()
            user.passwordResetToken = cryptoService.hashEncode(passwordResetToken)
            user.passwordResetDate = new Date()
            userService.saveDetached(user)

            String passwordResetLink = getPasswordResetLink(passwordRedirectUri, passwordResetToken, email)

            MailMessage message = new MailMessage()
            message.to = email
            message.subject = "${appName}: Password Reset"
            message.template = 'password-reset.ftl'
            message.model = [appName:appName, passwordResetLink:passwordResetLink]
            mailService.send(message)
        }
    }

    void reset(@NotNull String email, @NotNull String token, @NotNull String password) {
        User user = userService.findByEmail(email)
        if (isPasswordResetTokenValid(user, token)) {
            user.password = password
            user.passwordCreatedDate = new Date()
            user.isVerifyRequired = true
            userService.saveDetached(user)
        } else {
            throw new PasswordResetTokenExpiredException()
        }
    }

    private boolean isPasswordResetTokenValid(User user, String token) {
        if (user?.passwordResetToken && user?.passwordResetDate) {
            Integer minutesAgo = Integer.MAX_VALUE
            use(TimeCategory) {
                minutesAgo = (new Date() - user.passwordResetDate).minutes
            }
            if (minutesAgo < passwordResetTokenExpiresMinutes) {
                if (user.passwordResetToken == cryptoService.hashEncode(token)) {
                    return true
                }
            }
        }
        return false
    }

    private void validateClientConfig(String passwordRedirectUri) {
        String clientRedirectUri = clientService.getPasswordResetRedirectUri(passwordRedirectUri)
        if (!clientRedirectUri) {
            throw new PasswordResetNotConfiguredException()
        }
    }

    private String getPasswordResetLink(String passwordRedirectUri, String token, String email) {
        final String QUESTION_MARK = '?'
        final String AMPERSAND = '&'
        String clientRedirectUri = clientService.getPasswordResetRedirectUri(passwordRedirectUri)
        String redirectUri
        if (clientRedirectUri.contains(QUESTION_MARK)) {
            redirectUri = clientRedirectUri + AMPERSAND
        } else {
            redirectUri = clientRedirectUri + QUESTION_MARK
        }
        return redirectUri + 'email=' + email + '&token=' + token
    }
}
