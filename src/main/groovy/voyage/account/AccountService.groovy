package voyage.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import voyage.security.user.User
import voyage.security.user.UserService

@Service
@Validated
class AccountService {
    private final UserService userService

    @Autowired
    AccountService(UserService userService) {
        this.userService = userService
    }

    User register(User userIn) {
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
        newUser = userService.saveDetached(newUser)
        return newUser
    }
}
