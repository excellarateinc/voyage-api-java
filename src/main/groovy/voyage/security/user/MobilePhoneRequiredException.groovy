package voyage.security.user

import org.springframework.http.HttpStatus
import voyage.common.error.AppException

/**
 * For use within the service layer to inform the caller that a Mobile Phone is required for a new account.
 * This exception class will be caught by an exception handler (ie DefaultExceptionHandler) and transformed
 * into a 400 Bad Request HTTP response.
 *
 * If no message is provided during construction of this class, then the default message will be used and provided back
 * to the web service consumer.
 */
class MobilePhoneRequiredException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'At least one mobile phone is required for a new account'

    MobilePhoneRequiredException() {
        this(DEFAULT_MESSAGE)
    }

    MobilePhoneRequiredException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        return HTTP_STATUS.value() + '_mobile_phone_required'
    }
}
