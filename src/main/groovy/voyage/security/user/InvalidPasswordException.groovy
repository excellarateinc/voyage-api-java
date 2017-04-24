package voyage.security.user

import org.springframework.http.HttpStatus
import voyage.common.error.AppException

/**
 * Created by user on 4/19/2017.
 */
class InvalidPasswordException extends AppException {
    private static final HTTP_STATUS  = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The password did not meet the requirements'
    private final String codeExtension

    InvalidPasswordException() {
        this(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    InvalidPasswordException(String message) {
        this(HTTP_STATUS, message)
    }

    InvalidPasswordException(String message, String codeExtension) {
        super(HTTP_STATUS, message)
        this.codeExtension = codeExtension
    }
    @Override
    String getErrorCode() {
        String code = HTTP_STATUS.value() + '_password_invalid'
        if (codeExtension) {
            code = code + '_' + codeExtension.toLowerCase()
        }
        return code
    }
}
