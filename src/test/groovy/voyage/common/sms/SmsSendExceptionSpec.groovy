package voyage.common.sms

import org.springframework.http.HttpStatus
import spock.lang.Specification
import voyage.common.error.AppException

class SmsSendExceptionSpec extends Specification {

    def 'default exception creates a 400 Bad Request exception'() {
        when:
            AppException ex = new SmsSendException()

        then:
            ex.httpStatus == HttpStatus.INTERNAL_SERVER_ERROR
            ex.errorCode == '500_sms_sending_failed'
            ex.message == 'Failure sending text message. Please contact support.'
    }

    def 'Override the exception message only affects the description'() {
        when:
            AppException ex = new SmsSendException('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.INTERNAL_SERVER_ERROR
            ex.errorCode == '500_sms_sending_failed'
            ex.message == 'TEST MESSAGE'
    }
}
