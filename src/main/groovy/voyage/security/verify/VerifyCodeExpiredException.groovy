package voyage.security.verify

import org.springframework.http.HttpStatus
import voyage.common.error.AppException
import voyage.common.error.ErrorUtils

class VerifyCodeExpiredException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The verification code provided has expired. ' +
            'Please request another verification code and try again.'

    VerifyCodeExpiredException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    VerifyCodeExpiredException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        ErrorUtils.getErrorCode(httpStatus.value(), 'verify_code_expired')
    }
}
