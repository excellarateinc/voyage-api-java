package voyage.security.verify

import org.springframework.http.HttpStatus
import spock.lang.Specification
import voyage.common.error.AppException

class InvalidVerificationPhoneNumberExceptionSpec extends Specification {

    def 'default exception creates a 400 Bad Request exception'() {
        when:
            AppException ex = new InvalidVerificationPhoneNumberException()

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_verify_phone_invalid'
            ex.message == 'The verification phone number is invalid. Please contact technical support for assistance.'
    }

    def 'Override the exception message only affects the description'() {
        when:
            AppException ex = new InvalidVerificationPhoneNumberException ('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_verify_phone_invalid'
            ex.message == 'TEST MESSAGE'
    }
}
