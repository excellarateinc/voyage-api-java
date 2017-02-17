package launchpad.security.account

import launchpad.account.AccountService
import launchpad.security.user.User
import launchpad.security.user.UserService
import spock.lang.Specification

class AccountServiceSpec extends Specification {
    User user
    UserService userService = Mock()
    AccountService accountService = new AccountService(userService)

    def setup() {
        user = new User(firstName:'LSS', lastName:'India')
    }

    def 'register - applies the values and calls the userService' () {
        setup:
            userService.save(_) >> user
        when:
            User savedUser = accountService.register(user)
        then:
            'LSS' == savedUser.firstName
            'India' == savedUser.lastName
            !savedUser.isDeleted
    }

}
