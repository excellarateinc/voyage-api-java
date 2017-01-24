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

    User save(@NotNull User user) {
        return userRepository.save(user)
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

    void sendVerifyCode(@NotNull User user, String verifyMethod, VerifyCodeType verifyCodeType) {
        if (verifyMethod == VerifyMethod.TEXT.toString()) {
            sendVerifyCodeToPhoneNumber(user, verifyCodeType)
        } else {
            sendVerifyCodeToEmail(user, verifyCodeType)
        }
    }


}
