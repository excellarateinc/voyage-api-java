package voyage.security.verify

import org.springframework.http.HttpStatus
import spock.lang.Specification
import voyage.common.error.AppException

class InvalidVerificationCodeExceptionSpec extends Specification {

    def 'default exception creates a 400 Bad Request exception'() {
        when:
            AppException ex = new InvalidVerificationCodeException()

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_verify_code_invalid'
            ex.message == 'The verification code provided is invalid.'
    }

    def 'Override the exception message only affects the description'() {
        when:
            AppException ex = new InvalidVerificationCodeException('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_verify_code_invalid'
            ex.message == 'TEST MESSAGE'
    }
}
