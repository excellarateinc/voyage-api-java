package voyage.security

import org.springframework.http.HttpStatus
import voyage.common.error.AppException

/**
 * Overriding the base RedirectMismatchException to avoid displaying the client redirect url in the error message
 */
class ClientRedirectMismatchException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The client redirect URL was not valid. To resolve this issue please use a valid redirect url.'

    ClientRedirectMismatchException() {
        this(DEFAULT_MESSAGE)
    }

    ClientRedirectMismatchException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        return HTTP_STATUS.value() + '_invalid_redirect'
    }
}
