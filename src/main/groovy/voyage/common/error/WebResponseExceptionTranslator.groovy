package voyage.common.error

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception
import org.springframework.stereotype.Component
import voyage.security.AppOAuth2Exception

@Component
class WebResponseExceptionTranslator implements org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator {
    private static final String CORS_ACCESS_CONTROL_ALLOW_ORIGIN = 'Access-Control-Allow-Origin'
    private static final String CORS_ACCESS_WILDCARD = '*'

    @Override
    ResponseEntity<AppOAuth2Exception> translate(Exception e) throws Exception {
        if (e instanceof OAuth2Exception) {
            OAuth2Exception oAuth2Exception = (OAuth2Exception) e
            return ResponseEntity
                    .status(oAuth2Exception.httpErrorCode)
                    .header(CORS_ACCESS_CONTROL_ALLOW_ORIGIN, CORS_ACCESS_WILDCARD)
                    .body(new AppOAuth2Exception(HttpStatus.valueOf(oAuth2Exception.httpErrorCode), oAuth2Exception.message))

        } else if (e instanceof AuthenticationException) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .header(CORS_ACCESS_CONTROL_ALLOW_ORIGIN, CORS_ACCESS_WILDCARD)
                    .body(new AppOAuth2Exception(HttpStatus.UNAUTHORIZED, e.message))
        }

        return ResponseEntity
                .badRequest()
                .header(CORS_ACCESS_CONTROL_ALLOW_ORIGIN, CORS_ACCESS_WILDCARD)
                .body(new AppOAuth2Exception(HttpStatus.BAD_REQUEST, e.message))
    }
}
