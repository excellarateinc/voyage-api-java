package voyage.security.error

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.springframework.http.HttpStatus
import voyage.common.error.AppException

/**
 * Overriding the base OAuth2Exception so that we can provide an alternate serializer to conform to the standard JSON
 * error object format for this app.
 */
@JsonSerialize(using = AppOAuth2ExceptionSerializer)
class AppOAuth2Exception extends AppException {
    AppOAuth2Exception(HttpStatus httpStatus, String message) {
        super(httpStatus, message)
    }
}
