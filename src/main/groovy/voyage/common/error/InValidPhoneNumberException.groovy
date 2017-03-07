package voyage.common.error

import org.springframework.http.HttpStatus

class InValidPhoneNumberException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'Invalid phone number, please use E.164 international phone format, like +16123366715.'

    InValidPhoneNumberException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    InValidPhoneNumberException(String message) {
        super(HTTP_STATUS, message)
    }
}
