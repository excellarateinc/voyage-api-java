package voyage.security.error

import org.springframework.http.HttpStatus

/**
 * This exception will be thrown when a client provides an invalid redirect URL during an implicit authorization request.
 */
class OAuth2ClientRedirectException extends AppOAuth2Exception {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST
    private static final String DEFAULT_MESSAGE = 'The client redirect URL was not valid. To resolve this issue please use a valid redirect url.'

    OAuth2ClientRedirectException() {
        this(DEFAULT_MESSAGE)
    }

    OAuth2ClientRedirectException(String message) {
        super(HTTP_STATUS, message)
    }
}
