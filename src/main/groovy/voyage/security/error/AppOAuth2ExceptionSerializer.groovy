package voyage.security.error

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import voyage.common.error.ErrorUtils

/**
 * Overrides the default OAuth2Exception JSON serializer with the standard error object format for this app.
 * This is used by the Spring Security OAuth2 Authorization & Resource servlet filters.
 *
 * NOTE: Spring MVC Controllers & DefaultExceptionHandler processes occur after all of the authentication is complete,
 * which is why we need a special process to handle exceptions for the OAuth2 Authorization & Resource servers.
 */
class AppOAuth2ExceptionSerializer extends StdSerializer<AppOAuth2Exception> {
    AppOAuth2ExceptionSerializer() {
        super(AppOAuth2Exception)
    }

    @Override
    void serialize(AppOAuth2Exception ex, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.with {
            writeStartArray()
                writeStartObject()
                    writeStringField('error', ErrorUtils.getErrorCode(ex.httpStatus.value()))
                    writeStringField('errorDescription', "${ex.httpStatus.value()} ${ex.httpStatus.reasonPhrase}. ${ex.message}")
                writeEndObject()
            writeEndArray()
        }
    }
}
