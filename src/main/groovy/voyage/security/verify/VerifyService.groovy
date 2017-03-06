package voyage.security.verify

import groovy.time.TimeCategory
import voyage.security.user.User
import voyage.security.user.UserPhone
import voyage.security.user.UserService
import voyage.common.sms.SmsMessage
import voyage.common.sms.AwsSmsService
import voyage.common.error.UnknownIdentifierException
import voyage.common.mail.MailMessage
import voyage.common.mail.MailService
import voyage.security.SecurityCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotNull

@Service
@Validated
class VerifyService {
    private static final Logger LOG = LoggerFactory.getLogger(VerifyService)

    @Value('${security.verify-code-expire-minutes}')
    private static int verifyCodeExpires

    @Value('${app.name}')
    private static String appName

    private final UserService userService
    private final MailService mailService
    private final AwsSmsService smsService

    @Autowired
    VerifyService(UserService userService, MailService mailService, AwsSmsService smsService) {
        this.userService = userService
        this.mailService = mailService
        this.smsService = smsService
    }

    List<VerifyMethod> getVerifyMethodsForCurrentUser() {
        User user = userService.currentUser
        List<VerifyMethod> verifyMethods = []
        if (user.email) {
            VerifyMethod verifyMethod = new VerifyMethod()
            verifyMethod.label = user.maskedEmail
            verifyMethod.verifyType = VerifyType.EMAIL
            verifyMethod.value = 1
            verifyMethods.add(verifyMethod)
        }
        user.phones?.each { userPhone ->
            VerifyMethod verifyMethod = new VerifyMethod()
            verifyMethod.label = userPhone.maskedPhoneNumber
            verifyMethod.value = userPhone.id
            verifyMethod.verifyType = VerifyType.TEXT
            verifyMethods.add(verifyMethod)
        }
        return verifyMethods
    }

    boolean verifyCurrentUser(@NotNull String code) {
        User user = userService.currentUser
        if (!user.isVerifyRequired) {
            LOG.info('User is already verified. Skipping user verification.')
            return true
        }
        if (user.verifyCodeExpired) {
            throw new VerifyCodeExpiredException()
        }
        if (user.verifyCode != code?.trim()) {
            throw new InvalidVerificationCodeException()
        }
        user.with {
            verifyCode = null
            verifyCodeExpiresOn = null
            isVerifyRequired = false
        }
        userService.saveDetached(user)
        return true
    }

    void sendVerifyCodeToCurrentUser(VerifyMethod verifyMethod) {
        User user = userService.currentUser
        if (verifyMethod.verifyType == VerifyType.EMAIL) {
            sendVerifyCodeToEmail(user)
        } else if (verifyMethod.verifyType == VerifyType.TEXT) {
            sendVerifyCodeToPhoneNumber(user, verifyMethod.value as long)
        } else {
            throw new InvalidVerificationMethodException()
        }
    }

    private sendVerifyCodeToEmail(@NotNull User user) {
        user.verifyCode = SecurityCode.userVerifyCode
        use(TimeCategory) {
            user.verifyCodeExpiresOn = new Date() + verifyCodeExpires.minutes
        }
        MailMessage mailMessage = getVerifyCodeEmailMessage(user)
        mailService.send(mailMessage)
        userService.saveDetached(user)
    }

    private sendVerifyCodeToPhoneNumber(@NotNull User user, @NotNull long userPhoneId) {
        UserPhone userPhone = user.phones?.find { it.id == userPhoneId }
        if (!userPhone) {
            throw new UnknownIdentifierException("Provided phone number doesn't exist")
        }
        user.verifyCode = SecurityCode.userVerifyCode
        use(TimeCategory) {
            user.verifyCodeExpiresOn = new Date() + verifyCodeExpires.minutes
        }
        SmsMessage smsMessage = new SmsMessage()
        smsMessage.to = user.phones.find { it.id == userPhoneId }
        smsMessage.text = "Your ${appName} verification code is: ${user.verifyCode}"
        smsService.send(smsMessage)
        userService.saveDetached(user)
    }

    private static MailMessage getVerifyCodeEmailMessage(@NotNull User user) {
        MailMessage mailMessage = new MailMessage()
        mailMessage.to = user.email
        mailMessage.model = ['user':user]
        mailMessage.subject = "${appName} Verification Code"
        mailMessage.template = 'account-verification.ftl'
        return mailMessage
    }
}
