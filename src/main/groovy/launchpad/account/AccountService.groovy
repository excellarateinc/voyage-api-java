package launchpad.account

import launchpad.security.user.User
import launchpad.security.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

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
        newUser = userService.save(newUser)
        return newUser
    }
}
