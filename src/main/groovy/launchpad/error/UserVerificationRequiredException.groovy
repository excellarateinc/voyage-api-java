package launchpad.error

import org.springframework.http.HttpStatus

class UserVerificationRequiredException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.FORBIDDEN
    private static final String DEFAULT_MESSAGE = 'User verification is required'

    UserVerificationRequiredException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    UserVerificationRequiredException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        return ErrorUtils.getErrorCode(HTTP_STATUS.value(), 'verify_user')
    }
}
