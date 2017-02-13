package voyage.error

import org.springframework.http.HttpStatus

/**
 * For use within the service layer to inform the caller that the given Username is already being used within the
 * User table. This exception class will be caught by an exception handler (ie DefaultExceptionHandler) and transformed
 * into a 400 Bad Request HTTP response.
 *
 * If no message is provided during construction of this class, then the default message will be used and provided back
 * to the web service consumer.
 */
class UsernameAlreadyInUseException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'Username already in use by another user. Please choose a different username'

    UsernameAlreadyInUseException() {
        this(DEFAULT_MESSAGE)
    }

    UsernameAlreadyInUseException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        return HTTP_STATUS.value() + '_username_already_in_use'
    }
}
