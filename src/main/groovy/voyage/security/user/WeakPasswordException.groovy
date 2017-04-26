package voyage.security.user

import org.springframework.http.HttpStatus
import voyage.common.error.AppException

class WeakPasswordException extends AppException {
    private static final HTTP_STATUS  = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The password did not meet the requirements.Password should contain 1 Upper case Character, 1 Lower Case Character, 1 Special Character and should not have any whitespace.'
    private final String codeExtension

    WeakPasswordException() {
        this(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    WeakPasswordException(String message) {
        this(HTTP_STATUS, message)
    }

    WeakPasswordException(String message, String codeExtension) {
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
