package voyage.error

import org.springframework.http.HttpStatus

class MailSendException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.INTERNAL_SERVER_ERROR
    private static final String DEFAULT_MESSAGE = 'Failure sending email. Please try again later.'

    MailSendException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    MailSendException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        ErrorUtils.getErrorCode(httpStatus.value(), 'email_sending_failed')
    }
}
