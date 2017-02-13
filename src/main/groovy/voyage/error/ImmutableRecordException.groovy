package voyage.error

import org.springframework.http.HttpStatus

class ImmutableRecordException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The requested record is immutable. No changes to this record are allowed.'

    ImmutableRecordException() {
        this(DEFAULT_MESSAGE)
    }

    ImmutableRecordException(String message) {
        super(HTTP_STATUS, message)
    }
}
