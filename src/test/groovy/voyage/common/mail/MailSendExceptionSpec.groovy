package voyage.common.mail

import org.springframework.http.HttpStatus
import spock.lang.Specification
import voyage.common.error.AppException

class MailSendExceptionSpec extends Specification {

    def 'default exception creates a 400 Bad Request exception'() {
        when:
            AppException ex = new MailSendException()

        then:
            ex.httpStatus == HttpStatus.INTERNAL_SERVER_ERROR
            ex.errorCode == '500_email_sending_failed'
            ex.message == 'Failure sending email.'
    }

    def 'Override the exception message only affects the description'() {
        when:
            AppException ex = new MailSendException ('TEST MESSAGE')

        then:
            ex.httpStatus == HttpStatus.INTERNAL_SERVER_ERROR
            ex.errorCode == '500_email_sending_failed'
            ex.message == 'TEST MESSAGE'
    }
}
