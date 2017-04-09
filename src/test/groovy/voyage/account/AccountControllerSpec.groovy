package voyage.account

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import voyage.security.user.User
import voyage.security.verify.VerifyService

class AccountControllerSpec extends Specification {
    User user
    User modifiedUser
    AccountService accountService = Mock(AccountService)
    VerifyService userVerifyService = Mock(VerifyService)
    AccountController accountController = new AccountController(accountService, userVerifyService)

    def setup() {
        user = new User(id:1, firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password')
        modifiedUser = new User(id:1, firstName:'firstName', lastName:'LastName', username:'username', email:'test@test.com', password:'password')
    }

    def 'Test to validate register method'() {
        when:
            ResponseEntity<User> response = accountController.register(user)
        then:
            1 * accountService.register(user) >> modifiedUser
            response != null
            HttpStatus.CREATED == response.statusCode
            '/v1/account' == response.headers.location[0]

        when:
            accountController.register(user)
        then:
            1 * accountService.register(user) >> { throw new Exception() }
            thrown(Exception)
    }
}
