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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import voyage.core.mail.MailMessage
import voyage.core.mail.MailService
import voyage.security.user.User
import voyage.security.user.UserPhone
import voyage.security.user.UserService

@Service
@Validated
class ProfileService {
    private final UserService userService
    private final MailService mailService

    @Value('${app.name}')
    private String appName

    @Autowired
    ProfileService(UserService userService, MailService mailService) {
        this.userService = userService
        this.mailService = mailService
    }

    User save(User userIn) {
        User newUser = new User()
        newUser.with {
            firstName = userIn.firstName
            lastName = userIn.lastName
            username = userIn.username
            email = userIn.email
            password = userIn.password
            isEnabled = true
            isVerifyRequired = true
        }

        if (userIn.phones) {
            newUser.phones = newUser.phones ?: []
            userIn.phones.each { phoneIn ->
                newUser.phones.add(new UserPhone(
                    id:phoneIn.id,
                    phoneType:phoneIn.phoneType,
                    phoneNumber:phoneIn.phoneNumber,
                ))
            }
        }

        newUser = userService.saveDetached(newUser)

        // Send the welcome e-mail to the email address
        if (newUser.email) {
            MailMessage message = new MailMessage()
            message.to = newUser.email
            message.subject = "Welcome to ${appName}"
            message.template = 'welcome.ftl'
            mailService.send(message)
        }

        return newUser
    }
}
