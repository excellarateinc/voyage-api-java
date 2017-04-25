package voyage.profile

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import voyage.common.mail.MailMessage
import voyage.common.mail.MailService
import voyage.security.user.User
import voyage.security.user.UserPhone
import voyage.security.user.UserService

@Service
@Validated
class ProfileService {
    private final UserService userService
    private final MailService mailService

    @Value('${app.name}')
    private String appName

    @Autowired
    ProfileService(UserService userService, MailService mailService) {
        this.userService = userService
        this.mailService = mailService
    }

    User save(User userIn) {
        User newUser = new User()
        newUser.with {
            firstName = userIn.firstName
            lastName = userIn.lastName
            username = userIn.username
            email = userIn.email
            password = userIn.password
            isEnabled = true
            isVerifyRequired = true
        }

        if (userIn.phones) {
            newUser.phones = newUser.phones ?: []
            userIn.phones.each { phoneIn ->
                newUser.phones.add(new UserPhone(
                    id:phoneIn.id,
                    phoneType:phoneIn.phoneType,
                    phoneNumber:phoneIn.phoneNumber,
                ))
            }
        }

        newUser = userService.saveDetached(newUser)

        // Send the welcome e-mail to the email address
        if (newUser.email) {
            MailMessage message = new MailMessage()
            message.to = newUser.email
            message.subject = "Welcome to ${appName}"
            message.template = 'welcome.ftl'
            mailService.send(message)
        }

        return newUser
    }
}
