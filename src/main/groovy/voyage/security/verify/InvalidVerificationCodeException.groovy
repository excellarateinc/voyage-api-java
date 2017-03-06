package voyage.security.verify

import org.springframework.http.HttpStatus
import voyage.common.error.AppException
import voyage.common.error.ErrorUtils

class InvalidVerificationCodeException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The verification code provided is invalid.'

    InvalidVerificationCodeException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    InvalidVerificationCodeException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        ErrorUtils.getErrorCode(httpStatus.value(), 'verify_code_invalid')
    }
}
