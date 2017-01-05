package launchpad.error

import org.springframework.http.HttpStatus

class TokenExpiredException extends AppException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The requested token is expired. Please generate a new token.'

    TokenExpiredException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE)
    }

    TokenExpiredException(String message) {
        super(HTTP_STATUS, message)
    }
}
