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
        user = userService.save(user)
        return user
    }
}
