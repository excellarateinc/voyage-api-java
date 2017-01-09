package launchpad.error

import org.springframework.http.HttpStatus

class ErrorUtils {
    private static final String UNDER_SCORE = '_'

    static String createErrorCode(int httpStatusCode) {
        HttpStatus httpStatus = HttpStatus.valueOf(httpStatusCode)
        String errorCode = httpStatus.value() + UNDER_SCORE + httpStatus.name()
        return formatErrorCode(errorCode)
    }

    static String formatErrorCode(String description) {
        return description.toLowerCase().replace(' ', UNDER_SCORE)
    }
}
