package voyage.security.user

import org.springframework.http.HttpStatus
import voyage.common.error.AppException
import voyage.common.error.ErrorUtils

class WeakPasswordException extends AppException {
    private static final HTTP_STATUS  = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The password did not meet the requirements. Password should contain 1 upper case character, ' +
            '1 lower case character, 1 special character and should not contain any whitespace.'

    WeakPasswordException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    WeakPasswordException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        ErrorUtils.getErrorCode(HTTP_STATUS.value(), 'weak_password')
    }
}
