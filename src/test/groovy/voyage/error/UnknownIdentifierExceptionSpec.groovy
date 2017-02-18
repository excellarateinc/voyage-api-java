package voyage.error

import org.springframework.http.HttpStatus
import spock.lang.Specification

class UnknownIdentifierExceptionSpec extends Specification {

    def 'default exception creates a 400 Bad Request exception'() {
        when:
            AppException ex = new UnknownIdentifierException()

        then:
            ex.httpStatus == HttpStatus.NOT_FOUND
            ex.errorCode == '404_unknown_identifier'
            ex.message == 'Unknown record identifier provided'
    }

    def 'Override the exception message only affects the description'() {
        when:
            AppException ex = new UnknownIdentifierException('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.NOT_FOUND
            ex.errorCode == '404_unknown_identifier'
            ex.message == 'TEST MESSAGE'
    }
}
