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

import org.passay.PasswordData
import org.passay.PasswordValidator
import org.passay.RuleResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import voyage.core.error.UnknownIdentifierException
import voyage.security.crypto.CryptoService
import voyage.security.role.RoleService

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Transactional
@Service
@Validated
class UserService {
    private final UserRepository userRepository
    private final CryptoService cryptoService
    private final PhoneService phoneService
    private final RoleService roleService
    private final PasswordValidator passwordValidator

    @Value('${security.user-roles.default-authority}')
    private String defaultUserRoleAuthority

    @Autowired
    UserService(UserRepository userRepository, CryptoService cryptoService, PhoneService phoneService,
                RoleService roleService, PasswordValidator passwordValidator) {
        this.userRepository = userRepository
        this.cryptoService = cryptoService
        this.phoneService = phoneService
        this.roleService = roleService
        this.passwordValidator = passwordValidator
    }

    static String getCurrentUsername() {
        String username = null
        Authentication authenticationToken = SecurityContextHolder.context.authentication
        if (authenticationToken?.principal instanceof UserDetails) {
            username = ((UserDetails)authenticationToken.principal).username
        } else if (authenticationToken?.principal instanceof String) {
            username = authenticationToken.principal
        }
        return username
    }

    User getCurrentUser() {
        String username = currentUsername
        if (username) {
            return findByUsername(username)
        }
        return null
    }

    void delete(@NotNull Long id) {
        User user = get(id)
        user.isDeleted = true
        userRepository.save(user)
    }

    User findByUsername(@NotNull String username) {
        return userRepository.findByUsernameAndIsDeletedFalse(username)
    }

    User get(@NotNull Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
        if (!user) {
            throw new UnknownIdentifierException()
        }
        return user
    }

    Iterable<User> listAll() {
        return userRepository.findAllByIsDeletedFalse()
    }

    User saveDetached(@Valid User userIn) {
        User user

        if (userIn.id) {
            user = get(userIn.id)
        } else {
            user = new User()
        }

        if (userIn.username != user.username) {
            if (!isUsernameUnique(userIn.username)) {
                throw new UsernameAlreadyInUseException()
            }
        }

        user.with {
            firstName = userIn.firstName
            lastName = userIn.lastName
            username = userIn.username
            email = userIn.email
            isEnabled = userIn.isEnabled
            isAccountExpired = userIn.isAccountExpired
            isAccountLocked = userIn.isAccountLocked
            isCredentialsExpired = userIn.isCredentialsExpired
            forceTokensExpiredDate = userIn.forceTokensExpiredDate
            failedLoginAttempts = userIn.failedLoginAttempts

            // Default to true for new accounts
            isVerifyRequired = user.id ? userIn.isVerifyRequired : true

            if (!user.roles) {
                user.roles = [
                    roleService.findByAuthority(defaultUserRoleAuthority),
                ]
            }
        }

        if (userIn.password != user.password) {
            RuleResult result = passwordValidator.validate(new PasswordData(userIn.password))
            if (!result?.valid) {
                throw new WeakPasswordException(result.details)
            }
            user.password = cryptoService.hashEncode(userIn.password)
            user.passwordCreatedDate = new Date()
        }

        applyPhones(user, userIn)

        // Require at least one PhoneType.MOBILE phone
        UserPhone mobilePhone = user.phones?.find { it.phoneType == PhoneType.MOBILE && !it.isDeleted }
        if (!mobilePhone) {
            throw new MobilePhoneRequiredException()
        }

        return userRepository.save(user)
    }

    boolean isUsernameUnique(String username, User user = null) {
        User matchingUser = userRepository.findByUsernameAndIsDeletedFalse(username)
        if (matchingUser == user) {
            return true
        }
        return matchingUser ? false : true
    }

    private void applyPhones(User user, User userIn) {
        if (!userIn?.phones) {
            return
        }

        // Prevent an attacker from overloading the database will millions of phones. Used primarily for the profile
        // services that are exposed to anyone able to create and update an profile
        if (userIn.phones.size() > 5) {
            throw new TooManyPhonesException()
        }

        userIn.phones.each { phoneIn ->
            UserPhone userPhone = null
            if (phoneIn.id) {
                userPhone = user.phones?.find {
                    it.id == phoneIn.id && !it.isDeleted
                }
            }
            if (!userPhone) {
                userPhone = user.phones?.find {
                    it.phoneNumber == phoneIn.phoneNumber && !it.isDeleted
                }
            }
            if (!userPhone) {
                userPhone = new UserPhone()
                userPhone.user = user
                if (user.phones) {
                    user.phones.add(userPhone)
                } else {
                    user.phones = [userPhone]
                }
            }

            userPhone.phoneType = phoneIn.phoneType
            userPhone.phoneNumber = phoneService.toE164(phoneIn.phoneNumber)
        }

        Iterable<UserPhone> phonesToDelete = getPhonesToDelete(user.phones, userIn.phones)
        phonesToDelete.each { phone ->
            phone.isDeleted = true
        }
    }

    private static List<UserPhone> getPhonesToDelete(Iterable<UserPhone> currentPhones, Iterable<UserPhone> newPhones) {
        List toDelete = []
        currentPhones.each { currentPhone ->
            UserPhone phoneMatch = (UserPhone) newPhones.find { newPhone ->
                currentPhone.id == newPhone.id || currentPhone.phoneNumber == newPhone.phoneNumber
            }
            if (!phoneMatch) {
                toDelete.add(currentPhone)
            }
        }
        return toDelete
    }
}
