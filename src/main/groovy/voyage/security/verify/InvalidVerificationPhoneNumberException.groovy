package voyage.security.verify

import org.springframework.http.HttpStatus
import voyage.common.error.AppException
import voyage.common.error.ErrorUtils

class InvalidVerificationPhoneNumberException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The verification phone number is invalid. ' +
            'Please contact technical support for assistance.'

    InvalidVerificationPhoneNumberException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    InvalidVerificationPhoneNumberException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        ErrorUtils.getErrorCode(httpStatus.value(), 'verify_phone_invalid')
    }
}
