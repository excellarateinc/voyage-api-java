package launchpad.error

import org.springframework.http.HttpStatus

class InvalidVerificationCodeException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The requested verification code has expired. Please request another verification code.'

    InvalidVerificationCodeException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    InvalidVerificationCodeException(String message) {
        super(HTTP_STATUS, message)
    }
}
