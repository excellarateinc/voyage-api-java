package launchpad.security.user

import launchpad.error.UnknownIdentifierException
import launchpad.mail.MailMessage
import launchpad.mail.MailService
import launchpad.security.token.Token
import launchpad.security.token.TokenService
import launchpad.security.token.TokenType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Transactional
@Service
@Validated
class UserService {
    private final UserRepository userRepository

    @Autowired
    MailService mailService

    @Autowired
    TokenService tokenService

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository
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

    User findUserByToken(@NotNull String tokenValue) {
        Token token = tokenService.findByValue(tokenValue)
        User user = get(token.entityId)
        return user
    }

    boolean validateUserByToken(@NotNull String tokenValue) {
        User user = findUserByToken(tokenValue)
        return (user != null)
    }

    User activate(@NotNull String tokenValue) {
        Token token = tokenService.findByValue(tokenValue)
        User user = get(token.entityId)
        user.isEnabled = true
        userRepository.save(user)
        token.expiresOn = new Date()
        tokenService.save(token)
        return user
    }

    void sendVerificationEmail(User user) {
        Token token = tokenService.generate(user, TokenType.EMAIL_VERIFICATION)
        MailMessage mailMessage = new MailMessage()
        mailMessage.to = user.email
        mailMessage.model = ['token':token, 'user':user]
        mailMessage.subject = 'Account information'
        mailMessage.template = 'email-verification.ftl'
        mailMessage.from = 'support@launchpad.com' //TODO: ignore this when default email configured in properties
        mailService.send(mailMessage)
    }

    void sendPasswordResetEmail(User user) {
        Token token = tokenService.generate(user, TokenType.RESET_PASSWORD)
        MailMessage mailMessage = new MailMessage()
        mailMessage.to = user.email
        mailMessage.model = ['token':token, 'user':user]
        mailMessage.subject = 'Password reset information'
        mailMessage.template = 'reset-password-email'
        mailMessage.from = 'noreply@launchpad.com' //TODO: ignore this when default email configured in properties
        mailService.send(mailMessage)
    }
}
