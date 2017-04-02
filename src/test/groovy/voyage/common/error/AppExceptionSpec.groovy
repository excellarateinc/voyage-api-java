package voyage.common.error

import org.springframework.http.HttpStatus
import spock.lang.Specification

class AppExceptionSpec extends Specification {

    def 'default AppException creates a 400 Bad Request exception'() {
        when:
            AppException ex = new AppException()

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_bad_request'
            !ex.message
    }

    def 'AppException with custom status and message creates a proper error code'() {
        when:
            AppException ex = new AppException(HttpStatus.NOT_FOUND, 'TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.NOT_FOUND
            ex.errorCode == '404_not_found'
            ex.message == 'TEST MESSAGE'
    }
}
