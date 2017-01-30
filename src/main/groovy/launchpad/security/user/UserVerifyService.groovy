package launchpad.security.user

import groovy.time.TimeCategory
import launchpad.error.InvalidVerificationCodeException
import launchpad.error.VerifyCodeExpiredException
import launchpad.mail.MailMessage
import launchpad.mail.MailService
import launchpad.security.SecurityCode
import launchpad.sms.SmsMessage
import launchpad.sms.SmsService
import launchpad.util.StringUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

import javax.validation.constraints.NotNull

@Transactional
@Service
@Validated
class UserVerifyService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService)

    @Value('${verify-code-expire-minutes}')
    private int verifyCodeExpires

    @Value('${app.name}')
    private int appName

    private final UserService userService
    private final MailService mailService
    private final SmsService smsService

    @Autowired
    UserVerifyService(UserService userService, MailService mailService, SmsService smsService) {
        this.userService = userService
        this.mailService = mailService
        this.smsService = smsService
    }

    List<VerifyMethod> getVerifyMethodsForCurrentUser() {
        User user = userService.loggedInUser
        List<VerifyMethod> verifyMethods = []
        if (user.email) {
            verifyMethods.add(VerifyMethod.EMAIL)
        }
        if (user.userPhones) {
            verifyMethods.add(VerifyMethod.TEXT)
        }
        return verifyMethods
    }

    boolean verifyCurrentUser(@NotNull String verifyCode) {
        User user = userService.loggedInUser
        if (!user.isVerifyRequired) {
            LOG.info('User is already verified. Skipping user verification.')
            return true
        }
        if (user.verifyCodeExpired) {
            throw new VerifyCodeExpiredException()
        }
        if (user.verifyCode != verifyCode?.trim()) {
            throw new InvalidVerificationCodeException()
        }
        user.with {
            verifyCode = null
            verifyCodeExpiresOn = null
            isVerifyRequired = false
        }
        userService.save(user)
        return true
    }

    void sendVerifyCodeToCurrentUser(long userPhoneId = null) {
        User user = userService.loggedInUser
        if (userPhoneId) {
            sendVerifyCodeToPhoneNumber(user, userPhoneId)
        } else {
            sendVerifyCodeToEmail(user)
        }
    }

    void sendVerifyCodeToEmail(@NotNull User user) {
        user.verifyCode = getSecurityCode(user)
        use(TimeCategory) {
            user.verifyCodeExpiresOn = new Date() + verifyCodeExpires.minutes
        }
        MailMessage mailMessage = getVerifyCodeEmailMessage(user)
        mailService.send(mailMessage)
        userService.save(user)
    }

    void sendVerifyCodeToPhoneNumber(@NotNull User user, long userPhoneId) {
        user.verifyCode = SecurityCode.getUserVerifyCode()
        use(TimeCategory) {
            user.verifyCodeExpiresOn = new Date() + verifyCodeExpires.minutes
        }
        SmsMessage smsMessage = new SmsMessage()
        smsMessage.to = user.phones.find{ it.id == userPhoneId }
        smsMessage.text = "Your ${appName} verification code is: ${user.verifyCode}"
        smsService.send(smsMessage)
        userService.save(user)
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
