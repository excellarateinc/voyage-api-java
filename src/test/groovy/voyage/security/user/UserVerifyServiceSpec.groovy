package voyage.security.user

import voyage.account.VerifyMethod
import voyage.account.VerifyType
import voyage.error.InvalidVerificationCodeException
import voyage.error.UnknownIdentifierException
import voyage.error.VerifyCodeExpiredException
import voyage.mail.MailService
import voyage.sms.SmsService
import spock.lang.Specification

class UserVerifyServiceSpec extends Specification {
    User user
    UserService userService = Mock()
    MailService mailService = Mock()
    SmsService smsService  = Mock()
    UserVerifyService userVerifyService = new UserVerifyService(userService, mailService, smsService)

    def setup() {
        user = new User(firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password')
        UserPhone userPhone = new UserPhone(id:1, phoneType:PhoneType.HOME, phoneNumber:'9999999999', user:user)
        Set<UserPhone> userPhones = []
        userPhones.add(userPhone)
        user.phones = userPhones
    }

    def 'getVerifyMethodsForCurrentUser - validate verify method for current user' () {
        setup:
            userService.currentUser >> user
        when:
            List<VerifyMethod> verifyMethods = userVerifyService.verifyMethodsForCurrentUser
        then:
            2 == verifyMethods.size()
            'te**@test.com' == verifyMethods.get(0).label
            verifyMethods.get(0).verifyType == VerifyType.EMAIL
            verifyMethods.get(0).verifyType != VerifyType.TEXT
    }

    def 'verifyCurrentUser - validate user is already verified' () {
        setup:
            user = new User(firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password',
                    isVerifyRequired:false,)
            userService.currentUser >> user
        when:
            boolean isVerifyCurrentUser = userVerifyService.verifyCurrentUser('code')
        then:
            true == isVerifyCurrentUser
    }

    def 'verifyCurrentUser - validate VerifyCodeExpiredException' () {
        setup:
            user = new User(firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password',
                    isVerifyRequired:true, verifyCodeExpiresOn:new Date() - 1,)
            userService.currentUser >> user
        when:
             userVerifyService.verifyCurrentUser('code')
        then:
            thrown(VerifyCodeExpiredException)
    }

    def 'verifyCurrentUser - validate InvalidVerificationCodeException' () {
        setup:
            user = new User(firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password',
                    isVerifyRequired:true,)
            userService.currentUser >> user
        when:
            userVerifyService.verifyCurrentUser('code')
        then:
            thrown(InvalidVerificationCodeException)
    }

    def 'sendVerifyCodeToCurrentUser - validate sendVerifyCodeToEmail' () {
        setup:
            VerifyMethod verifyMethod = new VerifyMethod()
            verifyMethod.label = user.maskedEmail
            verifyMethod.verifyType = VerifyType.EMAIL
            userService.currentUser >> user
        when:
            userVerifyService.sendVerifyCodeToCurrentUser(verifyMethod)
        then:
            user.verifyCode
            user.verifyCodeExpiresOn.format('MM/DD/YYYY') == new Date().format('MM/DD/YYYY')
    }

    def 'sendVerifyCodeToCurrentUser - validate sendVerifyCodeToPhone' () {
        setup:

            VerifyMethod verifyMethod = new VerifyMethod()
            verifyMethod.label = user.phones[0].maskedPhoneNumber
            verifyMethod.value = user.phones[0].id
            verifyMethod.verifyType = VerifyType.TEXT
            userService.currentUser >> user
        when:
            userVerifyService.sendVerifyCodeToCurrentUser(verifyMethod)
        then:
            user.verifyCode
            user.verifyCodeExpiresOn.format('MM/DD/YYYY') == new Date().format('MM/DD/YYYY')
    }

    def 'sendVerifyCodeToCurrentUser - validate sendVerifyCodeToPhone with UnknownIdentifierException' () {
        setup:
            VerifyMethod verifyMethod = new VerifyMethod()
            verifyMethod.label = user.phones[0].maskedPhoneNumber
            verifyMethod.value = '2'
            verifyMethod.verifyType = VerifyType.TEXT
            userService.currentUser >> user
        when:
            userVerifyService.sendVerifyCodeToCurrentUser(verifyMethod)
        then:
            thrown(UnknownIdentifierException)
    }

}
