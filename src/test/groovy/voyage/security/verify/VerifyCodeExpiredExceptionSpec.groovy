package voyage.security.verify

import org.springframework.http.HttpStatus
import spock.lang.Specification
import voyage.common.error.AppException

class VerifyCodeExpiredExceptionSpec extends Specification {

    def 'default exception creates a 400 Bad Request exception'() {
        when:
            AppException ex = new VerifyCodeExpiredException()

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_verify_code_expired'
            ex.message == 'The verification code provided has expired. Please request another verification code and try again.'
    }

    def 'Override the exception message only affects the description'() {
        when:
            AppException ex = new VerifyCodeExpiredException('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_verify_code_expired'
            ex.message == 'TEST MESSAGE'
    }
}
