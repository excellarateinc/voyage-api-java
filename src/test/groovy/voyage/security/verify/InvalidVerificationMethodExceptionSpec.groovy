package voyage.security.verify

import org.springframework.http.HttpStatus
import spock.lang.Specification
import voyage.common.error.AppException

class InvalidVerificationMethodExceptionSpec extends Specification {

    def 'default exception creates a 400 Bad Request exception'() {
        when:
            AppException ex = new InvalidVerificationMethodException()

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_verify_method_invalid'
            ex.message == 'The verification method provided is invalid. Please select a valid verification method and try again.'
    }

    def 'Override the exception message only affects the description'() {
        when:
            AppException ex = new InvalidVerificationMethodException ('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_verify_method_invalid'
            ex.message == 'TEST MESSAGE'
    }
}
