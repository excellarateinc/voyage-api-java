package launchpad.error

import org.springframework.http.HttpStatus

class ResetPasswordCodeExpiredException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The requested email verification code is expired. Please request verification code again.'

    ResetPasswordCodeExpiredException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    ResetPasswordCodeExpiredException(String message) {
        super(HTTP_STATUS, message)
    }
}
