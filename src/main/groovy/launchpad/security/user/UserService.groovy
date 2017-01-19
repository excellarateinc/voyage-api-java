package launchpad.security.user

import launchpad.error.InvalidVerificationCodeException
import launchpad.error.ResetPasswordCodeExpiredException
import launchpad.error.UnknownIdentifierException
import launchpad.error.VerifyEmailCodeExpiredException
import launchpad.mail.MailMessage
import launchpad.mail.MailService
import launchpad.util.CryptoUtil
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
    @Value('${verifyEmailCodeExpireDays}')
    private int verifyEmailCodeExpireDays
    @Value('${resetPasswordCodeExpireDays}')
    private int resetPasswordCodeExpireDays

    private final UserRepository userRepository
    private final MailService mailService

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
            verifyEmailCode = CryptoUtil.generateUniqueToken(6)
            verifyEmailExpiresOn = new Date() + verifyEmailCodeExpireDays
        }
        user = userRepository.save(user)
        sendVerificationEmail(user)
        return user
    }

    User findByResetPasswordCode(@NotNull String tokenValue) {
        return userRepository.findByResetPasswordCode(tokenValue)
    }

    User verify(@NotNull String tokenValue) {
        User user = loggedInUser
        if (!user.isVerifyRequired) {
            LOG.info('User is already activated. Skipping user activation.')
            return user
        }
        if (user.isVerifyEmailCodeExpired()) {
            throw new VerifyEmailCodeExpiredException()
        }
        if (user.verifyEmailCode != tokenValue) {
            throw new InvalidVerificationCodeException()
        }
        user.with {
            verifyEmailCode = null
            verifyEmailExpiresOn = null
            isVerifyRequired = false
        }
        userRepository.save(user)
        return user
    }

    User resetPassword(@NotNull String resetPasswordCode, @NotNull String password) {
        User user = findByResetPasswordCode(resetPasswordCode)
        if (!user) {
            throw new UnknownIdentifierException()
        }
        if (user.isResetPasswordCodeExpired()) {
            throw new ResetPasswordCodeExpiredException()
        }
        if (user.resetPasswordCode != resetPasswordCode) {
            throw new InvalidVerificationCodeException()
        }
        user.password = password
        user.resetPasswordCode = null
        user.resetPasswordExpiresOn = null
        userRepository.save(user)
        return user
    }

    void sendVerificationEmail(User user) {
        if (!user) {
            throw new UnknownIdentifierException()
        }
        MailMessage mailMessage = new MailMessage()
        mailMessage.to = user.email
        mailMessage.model = ['user':user]
        mailMessage.subject = 'Account information'
        mailMessage.template = 'email-verification.ftl'
        mailService.send(mailMessage)
    }

    void sendPasswordResetEmail(User user) {
        if (!user) {
            throw new UnknownIdentifierException()
        }
        user.resetPasswordCode = CryptoUtil.generateUniqueToken(6)
        user.resetPasswordExpiresOn = new Date() + resetPasswordCodeExpireDays
        userRepository.save(user)
        MailMessage mailMessage = new MailMessage()
        mailMessage.to = user.email
        mailMessage.model = ['user':user]
        mailMessage.subject = 'Password reset information'
        mailMessage.template = 'reset-password-email'
        mailService.send(mailMessage)
    }
}
