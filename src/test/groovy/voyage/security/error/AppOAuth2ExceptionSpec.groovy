package voyage.security.error

import org.springframework.http.HttpStatus
import spock.lang.Specification
import voyage.common.error.AppException

class AppOAuth2ExceptionSpec extends Specification {

    def 'AppOAuth2Exception handles a 400 Bad Request properly'() {
        when:
            AppException ex = new AppOAuth2Exception(HttpStatus.BAD_REQUEST, 'test message')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_bad_request'
            ex.message == 'test message'
    }
}
