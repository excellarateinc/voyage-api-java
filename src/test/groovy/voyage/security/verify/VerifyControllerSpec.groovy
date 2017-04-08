package voyage.security.verify

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import voyage.security.user.User

class VerifyControllerSpec extends Specification {
    User user
    User modifiedUser
    VerifyService verifyService = Mock(VerifyService)
    VerifyController verifyController = new VerifyController(verifyService)
    
    def setup() {
        user = new User(id:1, firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password')
        modifiedUser = new User(id:1, firstName:'firstName', lastName:'LastName', username:'username', email:'test@test.com', password:'password')
    }

    def 'Test to validate sendVerificationCode method'() {
        when:
            ResponseEntity response = verifyController.sendVerificationCode()
        then:
            1 * verifyService.sendVerifyCodeToCurrentUser()
            HttpStatus.NO_CONTENT == response.statusCode

        when:
            verifyController.sendVerificationCode()
        then:
            1 * verifyService.sendVerifyCodeToCurrentUser() >> { throw new Exception() }
            thrown(Exception)
    }

    def 'Test to validate verify method'() {
        when:
            ResponseEntity response = verifyController.verify([code: 'code'])
        then:
            1 * verifyService.verifyCurrentUser('code')
            HttpStatus.NO_CONTENT == response.statusCode

        when:
            verifyController.verify([code: 'code'])
        then:
            1 * verifyService.verifyCurrentUser('code') >> { throw new Exception() }
            thrown(Exception)
    }
}
