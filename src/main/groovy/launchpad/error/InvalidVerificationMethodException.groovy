package launchpad.error

import org.springframework.http.HttpStatus

class InvalidVerificationMethodException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The verification method provided is invalid. Please select a valid verification method and try again.'

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
