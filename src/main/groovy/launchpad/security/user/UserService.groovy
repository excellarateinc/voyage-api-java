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

    private final UserRepository userRepository
    private final MailService mailService

    @Autowired
    UserService(UserRepository userRepository, MailService mailService) {
        this.userRepository = userRepository
        this.mailService = mailService
    }

    User getLoggedInUser() {
        String username
        Authentication authenticationToken = SecurityContextHolder.getContext().getAuthentication();
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
        user.firstName = userMap.firstName
        user.lastName = userMap.lastName
        user.username = userMap.username
        user.email = userMap.email
        user.password = userMap.password
        user.isEnabled = true
        user.isVerifyRequired = true
        user.verifyEmailCode = CryptoUtil.generateUniqueToken()
        user.verifyEmailExpiresOn = new Date() + 2 //TODO: get this value from the properties file
        user = userRepository.save(user)
        sendVerificationEmail(user)
        return user
    }

    User findByResetPasswordCode(@NotNull String tokenValue) {
        User user = userRepository.findByResetPasswordCode(tokenValue)
        return user
    }

    User activate(@NotNull String tokenValue) {
        User user = getLoggedInUser()
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
        userRepository.save(user)
        user.verifyEmailCode = null
        user.verifyEmailExpiresOn = null
        user.isVerifyRequired = false
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
        user.resetPasswordCode = CryptoUtil.generateUniqueToken()
        user.resetPasswordExpiresOn = new Date() + 2  //TODO: get this value from the properties file
        userRepository.save(user)
        MailMessage mailMessage = new MailMessage()
        mailMessage.to = user.email
        mailMessage.model = ['user':user]
        mailMessage.subject = 'Password reset information'
        mailMessage.template = 'reset-password-email'
        mailService.send(mailMessage)
    }
}
