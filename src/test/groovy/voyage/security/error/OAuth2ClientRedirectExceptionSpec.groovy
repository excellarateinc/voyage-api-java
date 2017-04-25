package voyage.security.error

import org.springframework.http.HttpStatus
import spock.lang.Specification
import voyage.common.error.AppException

class OAuth2ClientRedirectExceptionSpec extends Specification {

    def 'default exception creates a 400 Bad Request exception'() {
        when:
            AppException ex = new OAuth2ClientRedirectException()

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_bad_request'
            ex.message == 'The client redirect URL was not valid. To resolve this issue please use a valid redirect url.'
    }

    def 'Override the exception message only affects the description'() {
        when:
            AppException ex = new OAuth2ClientRedirectException('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_bad_request'
            ex.message == 'TEST MESSAGE'
    }
}
