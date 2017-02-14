package launchpad.security.account

import launchpad.account.AccountController
import launchpad.account.AccountService
import launchpad.account.VerifyMethod
import launchpad.account.VerifyType
import launchpad.security.user.User
import launchpad.security.user.UserService
import launchpad.security.user.UserVerifyService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

// TODO Needs to be rewritten to focus on inputs and outputs. Right now these tests are not validating the entire JSON response or JSON request
// TODO Remove the existing Exception tests because they do NOTHING! What's the point of these at all?
// TODO Add NEW exception tests for exceptions that are actually thrown by the Service classes (ImmutableRecordException,
//      UnknownIdentifierException, ValidationException...)
class AccountControllerSpec extends Specification {
    User user
    User modifiedUser
    AccountService accountService = Mock(AccountService)
    UserVerifyService userVerifyService = Mock(UserVerifyService)
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

    def 'Test to validate verifyMethods method'() {
        when:
            ResponseEntity<VerifyMethod> verifyMethods = accountController.verifyMethods()
        then:
            1 * userVerifyService.verifyMethodsForCurrentUser >> [verifyMethod]
            verifyMethods != null
            HttpStatus.OK == verifyMethods.statusCode
            VerifyType.EMAIL == verifyMethods.body[0].verifyType

        when:
            accountController.verifyMethods()
        then:
            1 * userVerifyService.verifyMethodsForCurrentUser >> { throw new Exception() }
            thrown(Exception)
    }

    def 'Test to validate sendVerificationCode method'() {
        when:
            ResponseEntity response = accountController.sendVerificationCode(verifyMethod)
        then:
            1 * userVerifyService.sendVerifyCodeToCurrentUser(verifyMethod)
            HttpStatus.NO_CONTENT == response.statusCode

        when:
            accountController.sendVerificationCode(verifyMethod)
        then:
            1 * userVerifyService.sendVerifyCodeToCurrentUser(verifyMethod) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'Test to validate verify method'() {
        when:
            ResponseEntity response = accountController.verify("code")
        then:
            1 * userVerifyService.verifyCurrentUser("code")
            HttpStatus.NO_CONTENT == response.statusCode

        when:
            accountController.verify("code")
        then:
            1 * userVerifyService.verifyCurrentUser("code") >> { throw new Exception() }
            thrown(Exception)
    }
}
