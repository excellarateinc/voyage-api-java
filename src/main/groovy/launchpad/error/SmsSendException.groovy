package launchpad.error

import org.springframework.http.HttpStatus

class SmsSendException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.INTERNAL_SERVER_ERROR
    private static final String DEFAULT_MESSAGE = 'Failure sending text message. Please try again later'

    SmsSendException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    SmsSendException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        ErrorUtils.getErrorCode(httpStatus.value(), 'sms_sending_failed')
    }
}
