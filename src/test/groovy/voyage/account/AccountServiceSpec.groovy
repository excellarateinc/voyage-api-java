package voyage.account

import spock.lang.Specification
import voyage.security.user.User
import voyage.security.user.UserService

class AccountServiceSpec extends Specification {
    User user
    UserService userService = Mock()
    AccountService accountService = new AccountService(userService)

    def setup() {
        user = new User(firstName:'LSS', lastName:'India')
    }

    def 'register - applies the values and calls the userService' () {
        setup:
            userService.saveDetached(_) >> user
        when:
            User savedUser = accountService.register(user)
        then:
            'LSS' == savedUser.firstName
            'India' == savedUser.lastName
            !savedUser.isDeleted
    }

}
