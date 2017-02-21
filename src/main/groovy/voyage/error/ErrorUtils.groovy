package voyage.error

import org.springframework.http.HttpStatus

class ErrorUtils {
    private static final String UNDER_SCORE = '_'

    static String getErrorCode(int httpStatusCode) {
        HttpStatus httpStatus = HttpStatus.valueOf(httpStatusCode)
        return getErrorCode(httpStatus.value(), httpStatus.name())
    }

    static String getErrorCode(int httpStatusCode, String description) {
        HttpStatus httpStatus = HttpStatus.valueOf(httpStatusCode)
        String errorCode = httpStatus.value() + UNDER_SCORE + description
        return formatErrorCode(errorCode)
    }

    static String formatErrorCode(String description) {
        return description.toLowerCase().replace(' ', UNDER_SCORE)
    }
}
