package voyage.security.verify

import spock.lang.Specification
import voyage.common.error.UnknownIdentifierException
import voyage.common.mail.MailService
import voyage.security.user.PhoneType
import voyage.security.user.User
import voyage.security.user.UserPhone
import voyage.security.user.UserService
import voyage.common.sms.AwsSmsService

class VerifyServiceSpec extends Specification {
    User user
    UserService userService = Mock()
    MailService mailService = Mock()
    AwsSmsService smsService  = Mock()
    VerifyService verifyService = new VerifyService(userService, mailService, smsService)

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
            List<VerifyMethod> verifyMethods = verifyService.verifyMethodsForCurrentUser
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
            boolean isVerifyCurrentUser = verifyService.verifyCurrentUser('code')
        then:
            isVerifyCurrentUser
    }

    def 'verifyCurrentUser - validate VerifyCodeExpiredException' () {
        setup:
            user = new User(firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password',
                    isVerifyRequired:true, verifyCodeExpiresOn:new Date() - 1,)
            userService.currentUser >> user
        when:
             verifyService.verifyCurrentUser('code')
        then:
            thrown(VerifyCodeExpiredException)
    }

    def 'verifyCurrentUser - validate InvalidVerificationCodeException' () {
        setup:
            user = new User(firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password',
                    isVerifyRequired:true,)
            userService.currentUser >> user
        when:
            verifyService.verifyCurrentUser('code')
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
            verifyService.sendVerifyCodeToCurrentUser(verifyMethod)
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
            verifyService.sendVerifyCodeToCurrentUser(verifyMethod)
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
            verifyService.sendVerifyCodeToCurrentUser(verifyMethod)
        then:
            thrown(UnknownIdentifierException)
    }
}
