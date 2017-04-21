package voyage.common

import org.springframework.http.HttpStatus
import spock.lang.Specification
import voyage.common.error.AppException

/**
 * Created by user on 4/19/2017.
 */
class InvalidPasswordExceptionSpec extends  Specification {
    def 'default exception creates a 400 Bad Request exception'() {
        when:
        InvalidPasswordException ex = new InvalidPasswordException()

        then:
        ex.httpStatus == HttpStatus.BAD_REQUEST
        ex.errorCode == '400_password_invalid'
        ex.message == 'The password did not met the requirements'
    }

    def 'Override the exception message only affects the description'() {
        when:
        InvalidPasswordException ex = new InvalidPasswordException('TEST MESSAGE')

        then:
        ex.httpStatus == HttpStatus.BAD_REQUEST
        ex.errorCode == '400_password_invalid'
        ex.message == 'TEST MESSAGE'
    }

    def 'Override the exception message and extend the code'() {
        when:
        InvalidPasswordException ex = new InvalidPasswordException('TEST MESSAGE', 'EXT')

        then:
        ex.httpStatus == HttpStatus.BAD_REQUEST
        ex.errorCode == '400_password_invalid_ext'
        ex.message == 'TEST MESSAGE'
    }
}
