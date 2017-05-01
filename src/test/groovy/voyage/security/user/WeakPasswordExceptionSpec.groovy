package voyage.security.user

import org.springframework.http.HttpStatus
import spock.lang.Specification

class WeakPasswordExceptionSpec extends  Specification {
    def 'default exception creates a 400 Bad Request exception'() {
        when:
            WeakPasswordException ex = new WeakPasswordException()

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_weak_password'
    }

    def 'Override the exception message only affects the description'() {
        when:
            WeakPasswordException ex = new WeakPasswordException('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_weak_password'
            ex.message == 'TEST MESSAGE'
    }
}
