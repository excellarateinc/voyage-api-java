package launchpad.security.user

import groovy.time.TimeCategory
import launchpad.error.InvalidVerificationCodeException
import launchpad.error.UnknownIdentifierException
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
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Transactional
@Service
@Validated
class UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService)

    @Value('${verify-code-expire-minutes}')
    private int verifyCodeExpires

    private final UserRepository userRepository
    private final MailService mailService
    @Autowired
    SmsService smsService

    @Autowired
    UserService(UserRepository userRepository, MailService mailService) {
        this.userRepository = userRepository
        this.mailService = mailService
    }

    User getLoggedInUser() {
        String username
        Authentication authenticationToken = SecurityContextHolder.context.authentication
        if (authenticationToken.principal instanceof UserDetails) {
            username = ((UserDetails)authenticationToken.principal).username
        } else if (authenticationToken.principal instanceof String) {
            username = authenticationToken.principal
        }
        if (username) {
            User user = findByUsername(username)
            return user
        }
    }

    void delete(@NotNull Long id) {
        User user = get(id)
        user.isDeleted = true
        userRepository.save(user)
    }

    User findByUsername(@NotNull String username) {
        return userRepository.findByUsername(username)
    }

    User get(@NotNull Long id) {
        User user = userRepository.findOne(id)
        if (!user) {
            throw new UnknownIdentifierException()
        }
        return user
    }

    Iterable<User> listAll() {
        return userRepository.findAll()
    }

    User saveDetached(@Valid User userIn) {
        if (userIn.id) {
            User user = get(userIn.id)
            user.with {
                firstName = userIn.firstName
                lastName = userIn.lastName
                username = userIn.username
                email = userIn.email
                password = userIn.password
                isEnabled = userIn.isEnabled
                isAccountExpired = userIn.isAccountExpired
                isAccountLocked = userIn.isAccountLocked
                isCredentialsExpired = userIn.isCredentialsExpired
            }
            return userRepository.save(user)
        }
        return userRepository.save(userIn)
    }

    User register(Map userMap) {
        User user = new User()
        user.with {
            firstName = userMap.firstName
            lastName = userMap.lastName
            username = userMap.username
            email = userMap.email
            password = userMap.password
            isEnabled = true
            isVerifyRequired = true
        }
        user = userRepository.save(user)
        return user
    }

    User verify(@NotNull String verifyCode, @NotNull User user) {
        if (!user.isVerifyRequired) {
            LOG.info('User is already activated. Skipping user activation.')
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
        userRepository.save(user)
        return user
    }

    User verifyPasswordRecoverCode(@NotNull String verifyCode) {
        User user = userRepository.findByVerifyCode(verifyCode)
        if (user.verifyCodeExpired) {
            throw new VerifyCodeExpiredException()
        }
        if (user.verifyCode != verifyCode) {
            throw new InvalidVerificationCodeException()
        }
        user.with {
            verifyCode = null
            verifyCodeExpiresOn = null
        }
        userRepository.save(user)
        return user
    }

    User resetPassword(@NotNull String verifyCode, @NotNull String password) {
        User user = userRepository.findByVerifyCode(verifyCode)
        if (!user) {
            throw new UnknownIdentifierException()
        }
        if (user.verifyCodeExpired) {
            throw new VerifyCodeExpiredException()
        }
        if (user.verifyCode != verifyCode) {
            throw new InvalidVerificationCodeException()
        }
        user.with {
            user.password = password
            user.verifyCode = null
            user.verifyCodeExpiresOn = null
        }
        userRepository.save(user)
        return user
    }

    List<VerifyMethod> getVerifyMethods(User user) {
        List<VerifyMethod> verifyMethods = []
        verifyMethods.add(VerifyMethod.EMAIL)
        if (user.phoneNumber) {
            verifyMethods.add(VerifyMethod.TEXT)
        }
        return verifyMethods
    }

    void sendVerifyCode(@NotNull User user, String verifyMethod, VerifyCodeType verifyCodeType) {
        if (verifyMethod == VerifyMethod.TEXT.toString()) {
            sendVerifyCodeToPhoneNumber(user, verifyCodeType)
        } else {
            sendVerifyCodeToEmail(user, verifyCodeType)
        }
    }

    void sendVerifyCodeToEmail(@NotNull User user, VerifyCodeType verifyCodeType) {
        user.verifyCode = getSecurityCode(user)
        use(TimeCategory) {
            user.verifyCodeExpiresOn = new Date() + verifyCodeExpires.minutes
        }
        MailMessage mailMessage = null
        if (verifyCodeType == VerifyCodeType.ACCOUNT_VERIFICATION) {
            mailMessage = getAccountVerificationEmailMessage(user)
        } else if (verifyCodeType == VerifyCodeType.PASSWORD_RESET) {
            mailMessage = getPasswordResetEmailMessage(user)
        }
        mailService.send(mailMessage)
        if (mailMessage.isEmailSent) {
            userRepository.save(user)
        }
    }

    void sendVerifyCodeToPhoneNumber(@NotNull User user, VerifyCodeType verifyCodeType) {
        user.verifyCode = getSecurityCode(user)
        use(TimeCategory) {
            user.verifyCodeExpiresOn = new Date() + verifyCodeExpires.minutes
        }
        SmsMessage smsMessage = new SmsMessage()
        smsMessage.to = user.phoneNumber

        if (verifyCodeType == VerifyCodeType.ACCOUNT_VERIFICATION) {
            smsMessage.text = "${user.verifyCode} is your account verification code"
        } else if (verifyCodeType == VerifyCodeType.PASSWORD_RESET) {
            smsMessage.text = "${user.verifyCode} is your account password recovery code"
        }
        smsService.send(smsMessage)
        if (smsMessage.isSmsSent) {
            userRepository.save(user)
        }
    }

    private static MailMessage getPasswordResetEmailMessage(@NotNull User user) {
        MailMessage mailMessage = new MailMessage()
        mailMessage.to = user.email
        mailMessage.model = ['user':user]
        mailMessage.subject = "${user.verifyCode} is your account verification code"
        mailMessage.template = 'email-verification.ftl'
        return mailMessage
    }

    private static MailMessage getAccountVerificationEmailMessage(@NotNull User user) {
        MailMessage mailMessage = new MailMessage()
        mailMessage.to = user.email
        mailMessage.model = ['user':user]
        mailMessage.subject = "${user.verifyCode} is your account password recovery code"
        mailMessage.template = 'reset-password-email.ftl'
        return mailMessage
    }

    private static String getSecurityCode(User user) {
        return user.username.take(4) + StringUtil.generateUniqueCode(6)
    }
}
