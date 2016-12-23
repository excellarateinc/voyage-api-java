package launchpad.error

import org.springframework.http.HttpStatus

/**
 * Basic exception that can be thrown by the application and will be caught by the DefaultExceptionHandler. This exception
 * will be translated into a general error back to the API consumer.
 */
class AppException extends RuntimeException {
    private HttpStatus httpStatus = HttpStatus.BAD_REQUEST

    AppException() {
        super()
    }

    AppException(HttpStatus httpStatus) {
        super()
        this.httpStatus = httpStatus
    }

    AppException(String message) {
        super(message)
    }

    AppException(HttpStatus httpStatus, String message) {
        super(message)
        this.httpStatus = httpStatus
    }

    HttpStatus getHttpStatus() {
        return httpStatus
    }
}

