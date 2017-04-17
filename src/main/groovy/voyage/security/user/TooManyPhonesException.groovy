package voyage.security.user

import org.springframework.http.HttpStatus
import voyage.common.error.AppException

/**
 * For use within the service layer to inform the caller that too many Phones have been added to the profile.
 * This exception class will be caught by an exception handler (ie DefaultExceptionHandler) and transformed
 * into a 400 Bad Request HTTP response.
 *
 * If no message is provided during construction of this class, then the default message will be used and provided back
 * to the web service consumer.
 */
class TooManyPhonesException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'Too many phones have been added to the profile. Maximum of 5.'

    TooManyPhonesException() {
        this(DEFAULT_MESSAGE)
    }

    TooManyPhonesException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        return HTTP_STATUS.value() + '_too_many_phones'
    }
}
