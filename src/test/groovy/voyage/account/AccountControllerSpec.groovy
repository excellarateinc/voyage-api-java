package voyage.account

import voyage.security.user.User
import voyage.security.verify.VerifyService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import voyage.security.verify.VerifyMethod
import voyage.security.verify.VerifyType

// TODO Needs to be rewritten to focus on inputs and outputs. Right now these tests are not validating the entire JSON response or JSON request
// TODO Remove the existing Exception tests because they do NOTHING! What's the point of these at all?
// TODO Add NEW exception tests for exceptions that are actually thrown by the Service classes (ImmutableRecordException,
//      UnknownIdentifierException, ValidationException...)
class AccountControllerSpec extends Specification {
    User user
    User modifiedUser
    AccountService accountService = Mock(AccountService)
    VerifyService userVerifyService = Mock(VerifyService)
    AccountController accountController = new AccountController(accountService, userVerifyService)
    VerifyMethod verifyMethod

    def setup() {
        user = new User(id:1, firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password')
        modifiedUser = new User(id:1, firstName:'firstName', lastName:'LastName', username:'username', email:'test@test.com', password:'password')
        verifyMethod = new VerifyMethod()
        verifyMethod.label = user.maskedEmail
        verifyMethod.verifyType = VerifyType.EMAIL
    }

    def 'Test to validate register method'() {
        when:
            ResponseEntity<User> response = accountController.register(user)
        then:
            1 * accountService.register(user) >> modifiedUser
            response != null
            HttpStatus.CREATED == response.statusCode
            '/v1/account/1' == response.headers.location[0]

        when:
            accountController.register(user)
        then:
            1 * accountService.register(user) >> { throw new Exception() }
            thrown(Exception)
    }
}
