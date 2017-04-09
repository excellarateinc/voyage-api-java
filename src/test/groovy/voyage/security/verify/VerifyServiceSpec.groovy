package voyage.security.verify

import spock.lang.Specification
import voyage.common.sms.AwsSmsService
import voyage.security.crypto.CryptoService
import voyage.security.user.PhoneType
import voyage.security.user.User
import voyage.security.user.UserPhone
import voyage.security.user.UserService

class VerifyServiceSpec extends Specification {
    User user
    UserService userService = Mock()
    CryptoService cryptoService = Mock()
    AwsSmsService smsService  = Mock()
    VerifyService verifyService = new VerifyService(userService, cryptoService, smsService)

    def setup() {
        verifyService.appName = 'Voyage'

        user = new User(firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password')
        user.phones = [
            new UserPhone(id:1, phoneType:PhoneType.HOME, phoneNumber:'123-123-1233', user:user),
            new UserPhone(id:2, phoneType:PhoneType.MOBILE, phoneNumber:'223-123-1233', user:user),
            new UserPhone(id:3, phoneType:PhoneType.MOBILE, phoneNumber:'323-123-1233', user:user),
        ]

        cryptoService.hashEncode(_ as String) >> { it -> return it }
        cryptoService.hashMatches(_ as String, _ as String) >> { plain, encoded -> return plain == encoded }
    }

    def 'verifyCurrentUser - validate user is already verified' () {
        setup:
            user.isVerifyRequired = false
            user.phones[2].verifyCode = 'code'
            user.phones[2].verifyCodeExpiresOn = new Date() + 1
            userService.currentUser >> user
        when:
            boolean isVerifyCurrentUser = verifyService.verifyCurrentUser('code')
        then:
            isVerifyCurrentUser
    }

    def 'verifyCurrentUser - validate VerifyCodeExpiredException' () {
        setup:
            user.isVerifyRequired = true
            user.phones[1].verifyCode = 'code'
            user.phones[1].verifyCodeExpiresOn = new Date() - 1
            userService.currentUser >> user
        when:
             verifyService.verifyCurrentUser('code')
        then:
            thrown(VerifyCodeExpiredException)
    }

    def 'verifyCurrentUser - validate InvalidVerificationCodeException' () {
        setup:
            user.isVerifyRequired = true
            user.phones[1].verifyCodeExpiresOn = new Date() + 1
            user.phones[1].verifyCode = 'code'
            userService.currentUser >> user
        when:
            verifyService.verifyCurrentUser('invalid-code')
        then:
            thrown(InvalidVerificationCodeException)
    }

    def 'sendVerifyCodeToCurrentUser - validate that it adds verification codes all mobile phone numbers and calls SMS' () {
        setup:
            user.isVerifyRequired = false
            userService.currentUser >> user
        when:
            verifyService.sendVerifyCodeToCurrentUser()
        then:
            user.isVerifyRequired
            user.phones.size() == 3

            user.phones[0].phoneType == PhoneType.HOME
            user.phones[0].phoneNumber == '123-123-1233'
            !user.phones[0].verifyCode
            !user.phones[0].verifyCodeExpiresOn

            user.phones[1].phoneType == PhoneType.MOBILE
            user.phones[1].phoneNumber == '223-123-1233'
            user.phones[1].verifyCode
            user.phones[1].verifyCodeExpiresOn <= new Date() + 30

            user.phones[2].phoneType == PhoneType.MOBILE
            user.phones[2].phoneNumber == '323-123-1233'
            user.phones[2].verifyCode != user.phones[1].verifyCode
            user.phones[2].verifyCodeExpiresOn <= new Date() + 30

            2 * smsService.send(_)
    }

    def 'sendVerifyCodeToCurrentUser - throws exception when no phone numbers are found' () {
        setup:
            user.isVerifyRequired = false
            user.phones = []
            userService.currentUser >> user
        when:
            verifyService.sendVerifyCodeToCurrentUser()
        then:
            !user.isVerifyRequired
            thrown(InvalidVerificationPhoneNumberException)
            0 * smsService.send(_)
    }

    def 'sendVerifyCodeToCurrentUser - throws exception when no mobile phone numbers are found' () {
        setup:
            user.isVerifyRequired = false
            user.phones = [
                new UserPhone(id:1, phoneType:PhoneType.HOME, phoneNumber:'123-123-1233', user:user),
            ]
            userService.currentUser >> user
        when:
            verifyService.sendVerifyCodeToCurrentUser()
        then:
            !user.isVerifyRequired
            thrown(InvalidVerificationPhoneNumberException)
            0 * smsService.send(_)
    }

    def 'sendVerifyCodeToCurrentUser - sends codes to a maximum of 5 phones' () {
        setup:
            user.isVerifyRequired = false
            user.phones = [
                    new UserPhone(id:1, phoneType:PhoneType.MOBILE, phoneNumber:'123-123-1233', user:user),
                    new UserPhone(id:2, phoneType:PhoneType.MOBILE, phoneNumber:'223-123-1233', user:user),
                    new UserPhone(id:3, phoneType:PhoneType.MOBILE, phoneNumber:'323-123-1233', user:user),
                    new UserPhone(id:4, phoneType:PhoneType.MOBILE, phoneNumber:'423-123-1233', user:user),
                    new UserPhone(id:5, phoneType:PhoneType.MOBILE, phoneNumber:'523-123-1233', user:user),
                    new UserPhone(id:6, phoneType:PhoneType.MOBILE, phoneNumber:'623-123-1233', user:user),
            ]
            userService.currentUser >> user
        when:
            verifyService.sendVerifyCodeToCurrentUser()
        then:
            user.isVerifyRequired
            user.phones.size() == 6

            user.phones[0].phoneType == PhoneType.MOBILE
            user.phones[0].phoneNumber == '123-123-1233'
            user.phones[0].verifyCode
            user.phones[0].verifyCodeExpiresOn

            user.phones[1].phoneType == PhoneType.MOBILE
            user.phones[1].phoneNumber == '223-123-1233'
            user.phones[1].verifyCode != user.phones[0].verifyCode
            user.phones[1].verifyCodeExpiresOn <= new Date() + 30

            user.phones[2].phoneType == PhoneType.MOBILE
            user.phones[2].phoneNumber == '323-123-1233'
            user.phones[2].verifyCode != user.phones[1].verifyCode
            user.phones[2].verifyCodeExpiresOn <= new Date() + 30

            user.phones[3].phoneType == PhoneType.MOBILE
            user.phones[3].phoneNumber == '423-123-1233'
            user.phones[3].verifyCode != user.phones[2].verifyCode
            user.phones[3].verifyCodeExpiresOn <= new Date() + 30

            user.phones[4].phoneType == PhoneType.MOBILE
            user.phones[4].phoneNumber == '523-123-1233'
            user.phones[4].verifyCode != user.phones[3].verifyCode
            user.phones[4].verifyCodeExpiresOn <= new Date() + 30

            user.phones[5].phoneType == PhoneType.MOBILE
            user.phones[5].phoneNumber == '623-123-1233'
            !user.phones[5].verifyCode
            !user.phones[5].verifyCodeExpiresOn

            5 * smsService.send(_)
    }

    def 'sendVerifyCodeToCurrentUser - sends proper SMS text message with code' () {
        setup:
            user.isVerifyRequired = false
            user.phones = [
                    new UserPhone(id:1, phoneType:PhoneType.MOBILE, phoneNumber:'123-123-1233', user:user),
                    new UserPhone(id:2, phoneType:PhoneType.MOBILE, phoneNumber:'223-123-1233', user:user),
            ]
            userService.currentUser >> user
        when:
            verifyService.sendVerifyCodeToCurrentUser()
        then:
            user.isVerifyRequired
            user.phones.size() == 2

            user.phones[0].phoneType == PhoneType.MOBILE
            user.phones[0].phoneNumber == '123-123-1233'
            user.phones[0].verifyCode
            user.phones[0].verifyCodeExpiresOn

            user.phones[1].phoneType == PhoneType.MOBILE
            user.phones[1].phoneNumber == '223-123-1233'
            user.phones[1].verifyCode != user.phones[0].verifyCode
            user.phones[1].verifyCodeExpiresOn <= new Date() + 30

            1 * smsService.send(*_) >> { args ->
                assert args[0].to == '123-123-1233'
                assert args[0].text ==~ /Your Voyage verification code is: \d+/
            }

            1 * smsService.send(*_) >> { args ->
                assert args[0].to == '223-123-1233'
                assert args[0].text ==~ /Your Voyage verification code is: \d+/
            }
    }
}
