package launchpad.error

import org.springframework.http.HttpStatus

class InvalidVerificationCodeException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The verification code provided is invalid. Please request another verification code and try again.'

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
