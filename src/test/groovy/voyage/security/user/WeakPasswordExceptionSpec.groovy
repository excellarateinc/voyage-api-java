package voyage.security.user

import org.springframework.http.HttpStatus
import spock.lang.Specification

class WeakPasswordExceptionSpec extends  Specification {
    def 'default exception creates a 400 Bad Request exception'() {
        when:
            WeakPasswordException ex = new WeakPasswordException()

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_week_password_the password did not meet the requirements.' +
                    'password should contain 1 upper case character, 1 lower case character, 1 special character and should not have any whitespace.'
    }

    def 'Override the exception message only affects the description'() {
        when:
            WeakPasswordException ex = new WeakPasswordException('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_week_password_test message'
    }

    def 'Override the exception message and extend the code'() {
        when:
            WeakPasswordException ex = new WeakPasswordException('TEST MESSAGE', 'EXT')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_week_password_ext'
            ex.message == 'TEST MESSAGE'
    }
}
