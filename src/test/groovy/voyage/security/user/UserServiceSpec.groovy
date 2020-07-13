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

import org.passay.PasswordValidator
import org.passay.RuleResult
import org.passay.RuleResultDetail
import spock.lang.Specification
import voyage.security.crypto.CryptoService
import voyage.security.role.Role
import voyage.security.role.RoleService

class UserServiceSpec extends Specification {
    User user
    User modifiedUser
    RuleResult validPasswordResult
    RuleResult invalidPasswordResult
    UserRepository userRepository = Mock()
    CryptoService cryptoService = Mock()
    PhoneService phoneService = new PhoneService()
    PasswordValidator passwordValidator =  Mock()
    RoleService roleService = Mock()
    UserService userService = new UserService(userRepository, cryptoService, phoneService, roleService, passwordValidator)

    def setup() {
        phoneService.defaultCountry = 'US'
        user = new User(
                username:'username', firstName:'LSS', lastName:'India', password:'Test&1234', isVerifyRequired:false,
                isEnabled:false, isAccountExpired:false, isAccountLocked:false, isCredentialsExpired:false,
        )
        user.phones = [new UserPhone(phoneNumber:'+16518886020', phoneType:PhoneType.MOBILE)]
        modifiedUser = new User(username:'username', firstName:'LSS', lastName:'Inc')
        validPasswordResult = new RuleResult(true)
        invalidPasswordResult = new RuleResult(false, new RuleResultDetail('1', ['1':'Missing something']))
    }

    def 'listAll - returns a single result' () {
        when:
            Iterable<User> userList = userService.listAll()
        then:
            userRepository.findAllByIsDeletedFalse() >> [user]
            1 == userList.size()
    }

    def 'save - creates new User and defaults isVerifyRequired to true'() {
        when:
            User savedUser = userService.saveDetached(user)
        then:
            userRepository.save(*_) >> { args ->
                return args[0] // return back the given user object
            }
            cryptoService.hashEncode(user.password) >> user.password
            passwordValidator.validate(_) >> validPasswordResult
            savedUser.username == 'username'
            savedUser.firstName == 'LSS'
            savedUser.lastName == 'India'
            savedUser.password == 'Test&1234'
            savedUser.isVerifyRequired
            !savedUser.isEnabled
            !savedUser.isAccountExpired
            !savedUser.isAccountLocked
            !savedUser.isCredentialsExpired
            !savedUser.isDeleted
    }

    def 'save - creates new User and sets a default security role'() {
        given:
            roleService.findByAuthority(_) >> { return new Role(authority:'test') }
        when:
            User savedUser = userService.saveDetached(user)
        then:
            userRepository.save(*_) >> { args ->
                return args[0] // return back the given user object
            }
            cryptoService.hashEncode(user.password) >> user.password
            passwordValidator.validate(_) >> validPasswordResult

            savedUser.username == 'username'
            1 == savedUser.roles.size()
            'test' == savedUser.roles[0].authority
    }

    def 'save - throws a Mobile Phone Required error because no phones are given'() {
        given:
            user.phones = []
        when:
            userService.saveDetached(user)
        then:
            passwordValidator.validate(_) >> validPasswordResult
            0 * userRepository.save(_)
            thrown(MobilePhoneRequiredException)
    }

    def 'save - throws a Mobile Phone Required error because phone list does not have a mobile phone type'() {
        given:
            user.id = 1
            user.phones[0].phoneType = PhoneType.HOME
        when:
            userService.saveDetached(user)
        then:
            1 * userRepository.findByIdAndIsDeletedFalse(user.id) >> user
            0 * userRepository.save(_)
            thrown(MobilePhoneRequiredException)
    }

    def 'save - throws a Mobile Phone Required error because no active phones are of type mobile'() {
        given:
            user.id = 1
            user.phones[0].phoneType = PhoneType.HOME
            User existingUser = new User(id:1, firstName:'test', lastName:'test', username:'username', password:'Test&1234')
            existingUser.phones = [new UserPhone(id:1, phoneNumber:'+16518886019', phoneType:PhoneType.MOBILE, isDeleted:false)]
        when:
            userService.saveDetached(user)
        then:
            1 * userRepository.findByIdAndIsDeletedFalse(existingUser.id) >> existingUser
            0 * userRepository.save(_)
            thrown(MobilePhoneRequiredException)
    }

    def 'save - updates an existing user if an ID is given'() {
        given:
            user.id = 1
        when:
            User userIn = new User(
                    id:1,
                    firstName:'FIRST',
                    lastName:'LAST',
                    username:'USERNAME',
                    password:'Test@1234',
                    isEnabled:true,
            )
            User updatedUser = userService.saveDetached(userIn)
        then:
            userRepository.findByIdAndIsDeletedFalse(user.id) >> user
            userRepository.save(user) >> user
            passwordValidator.validate(_) >> validPasswordResult
            updatedUser.isEnabled
            updatedUser.firstName == 'FIRST'
            updatedUser.lastName == 'LAST'
            updatedUser.username == 'USERNAME'
            !updatedUser.forceTokensExpiredDate
    }

    def 'save - fetches an existing user if an ID is given and updates select fields'() {
        given:
            user.id = 1
            User userIn = new User(
                    id:1,
                    firstName:'FIRST',
                    lastName:'LAST',
                    username:'USERNAME',
                    password:'Test&1234',
                    isEnabled:true,
                    isVerifyRequired:true,
                    isAccountExpired:true,
                    isAccountLocked:true,
                    isCredentialsExpired:true,
                    forceTokensExpiredDate:new Date(),
            )
        when:
            User updatedUser = userService.saveDetached(userIn)
        then:
            userRepository.findByIdAndIsDeletedFalse(user.id) >> user
            userRepository.save(user) >> user
            cryptoService.hashEncode(userIn.password) >> 'Test&1234'

            updatedUser.firstName == 'FIRST'
            updatedUser.lastName == 'LAST'
            updatedUser.username == 'USERNAME'
            updatedUser.password == 'Test&1234'
            updatedUser.isEnabled
            updatedUser.isVerifyRequired
            updatedUser.isAccountExpired
            updatedUser.isAccountLocked
            updatedUser.isCredentialsExpired
            updatedUser.forceTokensExpiredDate == userIn.forceTokensExpiredDate
    }

    def 'save - updating user with no password change does not re-encode the password'() {
        given:
            user.id = 1
            User userIn = new User(
                    id:1,
                    firstName:'FIRST',
                    lastName:'LAST',
                    username:'USERNAME',
                    password:'Test&1234',
            )
        when:
            User updatedUser = userService.saveDetached(userIn)
        then:
            userRepository.findByIdAndIsDeletedFalse(user.id) >> user
            userRepository.save(user) >> user
            0 * cryptoService.hashEncode(_) // No password encoding

            updatedUser.firstName == 'FIRST'
            updatedUser.lastName == 'LAST'
            updatedUser.username == 'USERNAME'
            updatedUser.password == 'Test&1234'
            updatedUser.isEnabled
            !updatedUser.isVerifyRequired
            !updatedUser.isAccountExpired
            !updatedUser.isAccountLocked
            !updatedUser.isCredentialsExpired
    }

    def 'save - updating user with multiple phones saves successfully'() {
        given:
            user.id = 1
            User userIn = new User(
                    id:1,
                    firstName:'FIRST',
                    lastName:'LAST',
                    username:'USERNAME',
                    password:'Test&1234',
            )
            userIn.phones = []
            userIn.phones.add(new UserPhone(phoneType:PhoneType.MOBILE, phoneNumber:'+1-651-888-6021'))
            userIn.phones.add(new UserPhone(phoneType:PhoneType.HOME, phoneNumber:'+1-651-888-6022'))
            userIn.phones.add(new UserPhone(phoneType:PhoneType.MOBILE, phoneNumber:'+1-651-888-6023'))
            userIn.phones.add(new UserPhone(phoneType:PhoneType.OFFICE, phoneNumber:'+1-651-888-6024'))
            userIn.phones.add(new UserPhone(phoneType:PhoneType.OTHER, phoneNumber:'+1-651-888-6025'))
        when:
            User updatedUser = userService.saveDetached(userIn)
        then:
            userRepository.findByIdAndIsDeletedFalse(user.id) >> user
            userRepository.save(user) >> user
            0 * cryptoService.hashEncode(_) // No password encoding
            passwordValidator.validate(_) >> validPasswordResult
            updatedUser.firstName == 'FIRST'
            updatedUser.lastName == 'LAST'
            updatedUser.phones.size() == 6
            updatedUser.phones[0].phoneNumber == '+16518886020'
            updatedUser.phones[0].phoneType == PhoneType.MOBILE
            updatedUser.phones[1].phoneNumber == '+16518886021'
            updatedUser.phones[1].phoneType == PhoneType.MOBILE
            updatedUser.phones[2].phoneNumber == '+16518886022'
            updatedUser.phones[2].phoneType == PhoneType.HOME
            updatedUser.phones[3].phoneNumber == '+16518886023'
            updatedUser.phones[3].phoneType == PhoneType.MOBILE
            updatedUser.phones[4].phoneNumber == '+16518886024'
            updatedUser.phones[4].phoneType == PhoneType.OFFICE
            updatedUser.phones[5].phoneNumber == '+16518886025'
            updatedUser.phones[5].phoneType == PhoneType.OTHER
    }

    def 'save - updating user with more than 5 phone numbers throws an exception'() {
        given:
            user.id = 1
            User userIn = new User(
                    id:1,
                    firstName:'FIRST',
                    lastName:'LAST',
                    username:'USERNAME',
                    password:'Test&1234',
            )
            userIn.phones = []
            userIn.phones.add(new UserPhone(phoneType:PhoneType.MOBILE, phoneNumber:'123-123-1233'))
            userIn.phones.add(new UserPhone(phoneType:PhoneType.MOBILE, phoneNumber:'223-123-1233'))
            userIn.phones.add(new UserPhone(phoneType:PhoneType.MOBILE, phoneNumber:'323-123-1233'))
            userIn.phones.add(new UserPhone(phoneType:PhoneType.MOBILE, phoneNumber:'423-123-1233'))
            userIn.phones.add(new UserPhone(phoneType:PhoneType.MOBILE, phoneNumber:'523-123-1233'))
            userIn.phones.add(new UserPhone(phoneType:PhoneType.MOBILE, phoneNumber:'623-123-1233'))
        when:
            userService.saveDetached(userIn)
        then:
            thrown(TooManyPhonesException)
            userRepository.findByIdAndIsDeletedFalse(user.id) >> user
            0 * userRepository.save(user)
            0 * cryptoService.hashEncode(_) // No password encoding
    }

    def 'save - mobile phone required exception when no phones with PhoneType=Mobile'() {
        given:
            user.id = 1
            user.phones = []
            user.phones.add(new UserPhone(id:1, phoneType:PhoneType.MOBILE, phoneNumber:'+16518886021'))
            user.phones.add(new UserPhone(id:2, phoneType:PhoneType.MOBILE, phoneNumber:'+16518886022'))
            user.phones.add(new UserPhone(id:3, phoneType:PhoneType.MOBILE, phoneNumber:'+16518886023'))

            User userIn = new User(
                    id:1,
                    firstName:'FIRST',
                    lastName:'LAST',
                    username:'USERNAME',
                    password:'Test&1234',
            )
            userIn.phones = []
            userIn.phones.add(new UserPhone(id:1, phoneType:PhoneType.HOME, phoneNumber:'+16518886021'))
            userIn.phones.add(new UserPhone(id:2, phoneType:PhoneType.HOME, phoneNumber:'+16518886022'))
            userIn.phones.add(new UserPhone(id:3, phoneType:PhoneType.HOME, phoneNumber:'+16518886023'))

        when:
            userService.saveDetached(userIn)
        then:
            userRepository.findByIdAndIsDeletedFalse(user.id) >> user
            0 * userRepository.save(user) >> user
            0 * cryptoService.hashEncode(_) // No password encoding

            thrown(MobilePhoneRequiredException)
    }

    def 'save - updating user phones replaces with the new given list'() {
        given:
            user.id = 1
            user.phones = []
            user.phones.add(new UserPhone(id:1, phoneType:PhoneType.MOBILE, phoneNumber:'+16518886021'))
            user.phones.add(new UserPhone(id:2, phoneType:PhoneType.MOBILE, phoneNumber:'+16518886022'))
            user.phones.add(new UserPhone(id:3, phoneType:PhoneType.MOBILE, phoneNumber:'+16518886023'))

            User userIn = new User(
                    id:1,
                    firstName:'FIRST',
                    lastName:'LAST',
                    username:'USERNAME',
                    password:'Test&1234',
            )
            userIn.phones = []
            userIn.phones.add(new UserPhone(id:2, phoneType:PhoneType.MOBILE, phoneNumber:'+16128886111'))
            userIn.phones.add(new UserPhone(phoneType:PhoneType.MOBILE, phoneNumber:'+16518886023'))
            userIn.phones.add(new UserPhone(phoneType:PhoneType.MOBILE, phoneNumber:'+16518886024'))

        when:
            User savedUser = userService.saveDetached(userIn)
        then:
            userRepository.findByIdAndIsDeletedFalse(user.id) >> user
            1 * userRepository.save(user) >> user
            0 * cryptoService.hashEncode(_) // No password encoding
            passwordValidator.validate(_) >> validPasswordResult
            savedUser.phones.size() == 4

            savedUser.phones[0].id == 1
            savedUser.phones[0].phoneNumber == '+16518886021'
            savedUser.phones[0].phoneType == PhoneType.MOBILE
            savedUser.phones[0].isDeleted

            savedUser.phones[1].id == 2
            savedUser.phones[1].phoneNumber == '+16128886111'
            savedUser.phones[1].phoneType == PhoneType.MOBILE
            !savedUser.phones[1].isDeleted

            savedUser.phones[2].id == 3
            savedUser.phones[2].phoneNumber == '+16518886023'
            savedUser.phones[2].phoneType == PhoneType.MOBILE
            !savedUser.phones[2].isDeleted

            savedUser.phones[3].id == null
            savedUser.phones[3].phoneNumber == '+16518886024'
            savedUser.phones[3].phoneType == PhoneType.MOBILE
            !savedUser.phones[3].isDeleted
    }

    def 'save - applies the values and calls the userRepository with UsernameAlreadyInUseException' () {
        when:
            userService.saveDetached(user)
        then:
            userRepository.findByUsernameAndIsDeletedFalse(_) >> user
            thrown(UsernameAlreadyInUseException)
    }

    def 'save - applies the values and calls the userRepository with existing user and UsernameAlreadyInUseException' () {
        when:
            User newUser = new User(username:'newusername', firstName:'LSS', lastName:'India')
            userService.saveDetached(newUser)
        then:
            userRepository.findByIdAndIsDeletedFalse(_) >> user
            userRepository.findByUsernameAndIsDeletedFalse(_) >> user
            thrown(UsernameAlreadyInUseException)
    }

    def 'get - calls the userRepository.findOne' () {
        when:
            User fetchedUser = userService.get(1)
        then:
            userRepository.findByIdAndIsDeletedFalse(_) >> user
            'LSS' == fetchedUser.firstName
            'India' == fetchedUser.lastName
            !fetchedUser.isDeleted
    }

    def 'delete - verifies the object and calls userRepository.delete' () {
        when:
            userService.delete(user.id)
        then:
            userRepository.findByIdAndIsDeletedFalse(_) >> user
            user.isDeleted
    }

    def 'update password - updating password with new password should compare passwords and saving user object'() {
        given:
            user.password = 'Efgh@5678'

        when:
            userService.saveDetached(user)

        then:
            cryptoService.encrypt('Test&1234' ) >> 'Test&1234'
            passwordValidator.validate(_) >> validPasswordResult
            1 * cryptoService.hashEncode('Efgh@5678')
            1 * userRepository.findByUsernameAndIsDeletedFalse('username')
            1 * userRepository.save(user)
    }

    def 'update password - updating password with same old password should not compare passwords '() {
        given:
            user.password = 'Test&1234'

        when:
            userService.saveDetached(user)

        then:
            cryptoService.encrypt('Test&1234' ) >> 'Test&1234'
            passwordValidator.validate(_) >> validPasswordResult
            0 * cryptoService.hashEncode('Efgh@5678')
            1 * userRepository.findByUsernameAndIsDeletedFalse('username')
            1 * userRepository.save(user)
    }

    def 'update password - updating password with null/blank value should give error'() {
        given:
            user.password = ''

        when:
            userService.saveDetached(user)

        then:
            passwordValidator.validate(_) >> invalidPasswordResult
            thrown(WeakPasswordException)
    }

    def 'update password - updating password with unsatisfied string combinations  should give error'() {
        given:
            user.password = 'password'

        when:
            userService.saveDetached(user)

        then:
            passwordValidator.validate(_) >> invalidPasswordResult
            thrown(WeakPasswordException)
    }
}
