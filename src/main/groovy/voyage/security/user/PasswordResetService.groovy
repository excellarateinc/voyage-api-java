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

    // Used for integration testing since threading and the use of secureRandom are difficult to simulate. This variable
    // is kept private intentionally so that tests using it will be exceeding visibility rights. See PasswordResetControllerIntegrationSpec.groovy
    protected boolean isTestingEnabled = false

    @Value('${app.name}')
    private String appName

    @Value('${app.contact-support.phone}')
    private String appContactPhone

    @Value('${app.contact-support.website}')
    private String appContactWebsite

    @Value('${security.password-verification.password-reset-token-expires-minutes}')
    private int passwordResetTokenExpiresMinutes

    @Value('${security.password-verification.password-reset-identity-server-redirect-url}')
    private String passwordResetIdentityServerRedirect

    @Autowired
    PasswordResetService(ClientService clientService, UserService userService, MailService mailService, CryptoService cryptoService) {
        this.clientService = clientService
        this.userService = userService
        this.mailService = mailService
        this.cryptoService = cryptoService
    }

    void sendIdentityServerResetMessage(@NotNull String email, String loginPageRedirectUri) {
        sendResetMessage(email, passwordResetIdentityServerRedirect, loginPageRedirectUri)
    }

    void sendApiResetMessage(@NotNull String email, @NotNull String emailRedirectUri) {
        String validatedEmailRedirectUri = findPasswordResetRedirectUri(emailRedirectUri)
        sendResetMessage(email, validatedEmailRedirectUri)
    }

    boolean isValidToken(@NotNull String email, @NotNull String token) {
        User user = userService.findByEmail(email)
        return isPasswordResetTokenValid(user, token)
    }

    void reset(@NotNull String email, @NotNull String token, @NotNull String password) {
        User user = userService.findByEmail(email)
        if (isPasswordResetTokenValid(user, token)) {
            user.password = password
            user.passwordResetToken = null
            user.passwordResetDate = null
            user.passwordResetLoginUri = null
            userService.save(user)

            MailMessage message = new MailMessage()
            message.to = email
            message.subject = "${appName}: Password Reset Successful"
            message.template = 'password-success.ftl'
            message.model = [appName:appName, website:appContactWebsite, phone:appContactPhone]
            mailService.send(message)

        } else {
            throw new PasswordResetTokenExpiredException()
        }
    }

    /*
     * Used by the API password reset workflow. This method will find all of the password redirect URIs associated with the
     * current client and then return the "best" one. The "best" will first be the URI that matches the given redirectUri,
     * and then will default to the first password reset URI within the list associated with the client record.
     *
     * An exception will be thrown if no valid password redirect URI is found because the email will not be able to be
     * sent without a URI to redirect the user.
     *
     * @param redirectUri
     * @return String URI
     */
    String findPasswordResetRedirectUri(String redirectUri) {
        String validRedirectUri = clientService.getPasswordResetRedirectUri(redirectUri)
        if (!validRedirectUri) {
            throw new PasswordResetNotConfiguredException()
        }
        return validRedirectUri
    }

    /*
     * Used by the identity server OAuth MVC password reset workflow. This method will find all of the login page edirect
     * URIs associated with the current client and then return the "best" one. The "best" will first be the URI that matches
     * the given redirectUri, and then will default to the first login page URI within the list associated with the
     * client record.
     *
     * An exception will be thrown if the given login page URI is not null and no valid URI is found. The reason is to
     * notify the consumer that the client login redirect URIs were not setup.
     *
     * @param redirectUri
     * @return String URI
     */
    String findLoginPageRedirectUri(String redirectUri) {
        String validRedirectUri = clientService.getLoginPageRedirectUri(redirectUri)
        if (redirectUri && !validRedirectUri) {
            throw new PasswordResetNotConfiguredException()
        }
        return validRedirectUri
    }

    /*
     *  Spawn a new thread so that the request returns the same response time for a valid or invalid email. An attacker
     *  could determine a valid email address from an invalid email by the duration of the response if the task is not
     *  completed within a separate thread allowing this method to return immediately.
     *
     *  Skipping the thread when in testing mode due to the inability to test the actions in a separate thread
     */
    private void sendResetMessage(String email, String emailRedirectUri, String loginRedirectUri = null) {
        if (isTestingEnabled) {
            sendResetMessageThreadTask(email, emailRedirectUri, loginRedirectUri)
        } else {
            Thread.start {
                sendResetMessageThreadTask(email, emailRedirectUri, loginRedirectUri)
            }
        }
    }

    private void sendResetMessageThreadTask(String email, String emailRedirectUri, String passwordResetLoginUri = null) {
        User user = userService.findByEmail(email)
        if (user) {
            String passwordResetToken = cryptoService.secureRandomToken()
            user.passwordResetToken = cryptoService.hashEncode(passwordResetToken)
            user.passwordResetDate = new Date()
            user.passwordResetLoginUri = passwordResetLoginUri
            userService.save(user)

            String passwordResetLink = getPasswordResetLink(emailRedirectUri, passwordResetToken, email)

            MailMessage message = new MailMessage()
            message.to = email
            message.subject = "${appName}: Password Reset"
            message.template = 'password.ftl'
            message.model = [appName:appName, website:appContactWebsite, phone:appContactPhone, passwordResetLink:passwordResetLink]
            mailService.send(message)
        }
    }

    private boolean isPasswordResetTokenValid(User user, String token) {
        if (user?.passwordResetToken && user?.passwordResetDate) {
            Calendar passwordResetDate = user.passwordResetDate.toCalendar()
            Calendar expiresDate = Calendar.instance
            expiresDate.add(Calendar.MINUTE, -passwordResetTokenExpiresMinutes)
            if (passwordResetDate.after(expiresDate)) {
                if (cryptoService.hashMatches(token, user.passwordResetToken)) {
                    return true
                }
            }
        }
        return false
    }

    private static String getPasswordResetLink(String emailRedirectUri, String token, String email) {
        final String QUESTION_MARK = '?'
        final String AMPERSAND = '&'
        String redirectUri
        if (emailRedirectUri.contains(QUESTION_MARK)) {
            redirectUri = emailRedirectUri + AMPERSAND
        } else {
            redirectUri = emailRedirectUri + QUESTION_MARK
        }

        // URL encode the parameter values to ensure they are browser friendly.
        String parameters = 'email=' + URLEncoder.encode(email, 'UTF-8')
        parameters += '&token=' + URLEncoder.encode(token, 'UTF-8')

        return redirectUri + parameters
    }
}
