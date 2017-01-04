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
@Service('userService')
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

    User save(@Valid User userIn) {
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

    void sendVerificationEmail(User user) {
        //TODO: Set expiry date for the token. Once user used that token set it to current date
        Token token = tokenService.generate(user, TokenType.EMAIL_VERIFICATION)
        MailMessage mailMessage = new MailMessage()
        mailMessage.to = user.email
        mailMessage.model = ['token':token, 'user':user]
        mailMessage.subject = 'Account information'
        mailMessage.template = 'email-verification.ftl'
        mailMessage.from = 'support@launchpad.com'
        mailService.send(mailMessage)
    }
}
