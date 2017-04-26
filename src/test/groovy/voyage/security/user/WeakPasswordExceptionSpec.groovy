package voyage.security.user

import org.springframework.http.HttpStatus
import spock.lang.Specification

class WeakPasswordExceptionSpec extends  Specification {
    def 'default exception creates a 400 Bad Request exception'() {
        when:
            WeakPasswordException ex = new WeakPasswordException()

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_password_invalid_the password did not meet the requirements'
    }

    def 'Override the exception message only affects the description'() {
        when:
            WeakPasswordException ex = new WeakPasswordException('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_password_invalid_test message'
    }

    def 'Override the exception message and extend the code'() {
        when:
            WeakPasswordException ex = new WeakPasswordException('TEST MESSAGE', 'EXT')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_password_invalid_ext'
            ex.message == 'TEST MESSAGE'
    }
}
