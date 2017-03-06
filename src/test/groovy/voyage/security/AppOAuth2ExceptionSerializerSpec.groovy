package voyage.security

import com.fasterxml.jackson.core.JsonGenerator
import org.springframework.http.HttpStatus
import spock.lang.Specification

class AppOAuth2ExceptionSerializerSpec extends Specification {

    def 'serialize exception as JSON'() {
        given:
            AppOAuth2Exception exception = new AppOAuth2Exception(HttpStatus.BAD_REQUEST, 'test message')
            AppOAuth2ExceptionSerializer serializer = new AppOAuth2ExceptionSerializer()
            JsonGenerator jsonGenerator = Mock(JsonGenerator)

        when:
            serializer.serialize(exception, jsonGenerator, null)

        then:
            1 * jsonGenerator.writeStartArray()
            1 * jsonGenerator.writeStartObject()
            1 * jsonGenerator.writeStringField('error', '400_bad_request')
            1 * jsonGenerator.writeStringField('errorDescription', '400 Bad Request. test message')
            1 * jsonGenerator.writeEndArray()
            1 * jsonGenerator.writeEndObject()
    }
}
