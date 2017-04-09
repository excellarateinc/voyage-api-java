package voyage.common

import org.springframework.http.HttpStatus
import voyage.common.error.AppException

class PhoneNumberInvalidException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The phone number provided is not recognized.'
    private final String codeExtension

    PhoneNumberInvalidException() {
        this(DEFAULT_MESSAGE)
    }

    PhoneNumberInvalidException(String message) {
        this(message, '')
    }

    PhoneNumberInvalidException(String message, String codeExtension) {
        super(HTTP_STATUS, message)
        this.codeExtension = codeExtension
    }

    @Override
    String getErrorCode() {
        String code = HTTP_STATUS.value() + '_phone_invalid'
        if (codeExtension) {
            code = code + '_' + codeExtension.toLowerCase()
        }
        return code
    }
}
