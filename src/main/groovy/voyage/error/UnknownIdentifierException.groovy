package voyage.error

import org.springframework.http.HttpStatus

/**
 * For use within the service layer to inform the caller that the given ID was not found within the database. This
 * exception class will be caught by an exception handler (ie DefaultExceptionHandler) and transformed into a 404 Not
 * Found HTTP response.
 *
 * If no message is provided during construction of this class, then the default message will be used and provided back
 * to the web service consumer.
 */
class UnknownIdentifierException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND
    private static final String DEFAULT_MESSAGE = 'Unknown record identifier provided'

    UnknownIdentifierException() {
        this(DEFAULT_MESSAGE)
    }

    UnknownIdentifierException(String message) {
        super(HTTP_STATUS, message)
    }

    @Override
    String getErrorCode() {
        return HTTP_STATUS.value() + '_unknown_identifier'
    }
}
