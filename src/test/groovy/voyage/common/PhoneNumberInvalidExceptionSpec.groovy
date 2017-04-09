package voyage.common

import org.springframework.http.HttpStatus
import spock.lang.Specification

class PhoneNumberInvalidExceptionSpec extends Specification {

    def 'default exception creates a 400 Bad Request exception'() {
        when:
            PhoneNumberInvalidException ex = new PhoneNumberInvalidException()

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_phone_invalid'
            ex.message == 'The phone number provided is not recognized.'
    }

    def 'Override the exception message only affects the description'() {
        when:
            PhoneNumberInvalidException ex = new PhoneNumberInvalidException('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_phone_invalid'
            ex.message == 'TEST MESSAGE'
    }

    def 'Override the exception message and extend the code'() {
        when:
            PhoneNumberInvalidException ex = new PhoneNumberInvalidException('TEST MESSAGE', 'EXT')

        then:
            ex.httpStatus == HttpStatus.BAD_REQUEST
            ex.errorCode == '400_phone_invalid_ext'
            ex.message == 'TEST MESSAGE'
    }
}
