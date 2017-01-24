package launchpad.security.user

import groovy.time.TimeCategory
import launchpad.error.InvalidVerificationCodeException
import launchpad.error.VerifyCodeExpiredException
import launchpad.mail.MailMessage
import launchpad.mail.MailService
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
        verifyMethods.add(VerifyMethod.EMAIL)
        if (user.phoneNumber) {
            verifyMethods.add(VerifyMethod.TEXT)
        }
        return verifyMethods
    }

    User verifyCurrentUser(@NotNull String verifyCode) {
        User user = userService.loggedInUser
        if (!user.isVerifyRequired) {
            LOG.info('User is already verified. Skipping user verification.')
            return user
        }
        if (user.verifyCodeExpired) {
            throw new VerifyCodeExpiredException()
        }
        if (user.verifyCode != verifyCode) {
            throw new InvalidVerificationCodeException()
        }
        user.with {
            verifyCode = null
            verifyCodeExpiresOn = null
            isVerifyRequired = false
        }
        userService.save(user)
        return user
    }

    void sendVerifyCodeToCurrentUser(String verifyMethod) {
        User user = userService.loggedInUser
        if (verifyMethod == VerifyMethod.TEXT.toString()) {
            sendVerifyCodeToPhoneNumber(user)
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
        if (mailMessage.isEmailSent) {
            userService.save(user)
        }
    }

    void sendVerifyCodeToPhoneNumber(@NotNull User user) {
        user.verifyCode = getSecurityCode(user)
        use(TimeCategory) {
            user.verifyCodeExpiresOn = new Date() + verifyCodeExpires.minutes
        }
        SmsMessage smsMessage = new SmsMessage()
        smsMessage.to = user.phoneNumber
        smsMessage.text = "Your Voyage verification code is: ${user.verifyCode}"
        smsService.send(smsMessage)
        if (smsMessage.isSmsSent) {
            userService.save(user)
        }
    }

    private static MailMessage getVerifyCodeEmailMessage(@NotNull User user) {
        MailMessage mailMessage = new MailMessage()
        mailMessage.to = user.email
        mailMessage.model = ['user':user]
        mailMessage.subject = 'Voyage Verification Code'
        mailMessage.template = 'account-verification.ftl'
        return mailMessage
    }

    private static String getSecurityCode(User user) {
        return user.username.take(4) + StringUtil.generateUniqueCode(6)
    }
}
