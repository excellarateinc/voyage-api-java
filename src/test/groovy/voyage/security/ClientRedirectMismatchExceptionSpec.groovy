package voyage.security

import org.springframework.http.HttpStatus
import spock.lang.Specification
import voyage.common.error.AppException

class ClientRedirectMismatchExceptionSpec extends Specification {

    def 'ClientRedirectMismatchExceptionSpec handles a 400 Bad Request properly'() {
        when:
        AppException ex = new ClientRedirectMismatchException('test message')

        then:
        ex.httpStatus == HttpStatus.BAD_REQUEST
        ex.errorCode == '400_invalid_redirect'
        ex.message == 'test message'
    }
}
