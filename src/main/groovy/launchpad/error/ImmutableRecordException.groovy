package launchpad.error

import org.springframework.http.HttpStatus

class ImmutableRecordException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND
    private static final String DEFAULT_MESSAGE = 'The requested record is immutable. No changes to this record are allowed.'

    ImmutableRecordException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    ImmutableRecordException(String message) {
        super(HTTP_STATUS, message)
    }
}
