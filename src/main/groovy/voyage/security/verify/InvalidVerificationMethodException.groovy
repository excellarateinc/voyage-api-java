package voyage.security.verify

import org.springframework.http.HttpStatus
import voyage.common.error.AppException
import voyage.common.error.ErrorUtils

class InvalidVerificationMethodException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The verification method provided is invalid. ' +
            'Please select a valid verification method and try again.'

    InvalidVerificationMethodException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    InvalidVerificationMethodException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        ErrorUtils.getErrorCode(httpStatus.value(), 'verify_method_invalid')
    }
}
