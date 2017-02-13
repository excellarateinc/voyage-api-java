package launchpad.error

import org.springframework.http.HttpStatus

class PasswordRecoveryFailedException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'Password recovery failed. Please try again later.'

    PasswordRecoveryFailedException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    PasswordRecoveryFailedException(String message) {
        super(HTTP_STATUS, message)
    }
}
