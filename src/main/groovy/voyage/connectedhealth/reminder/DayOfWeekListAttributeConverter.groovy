package voyage.connectedhealth.reminder

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

import javax.persistence.AttributeConverter

class DayOfWeekListAttributeConverter implements AttributeConverter<List<DayOfWeek>, String> {
    private static final TypeReference<List<DayOfWeek>> TypeRef = new TypeReference<List<DayOfWeek>>() {}

    private final ObjectMapper objectMapper = new ObjectMapper()

    @Override
    String convertToDatabaseColumn(List<DayOfWeek> attribute) {
        if (!attribute) {
            return null
        }
        try {
            return objectMapper.writeValueAsString(attribute)
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }
    }

    @Override
    List<DayOfWeek> convertToEntityAttribute(String dbData) {
        if (!dbData) {
            return null
        }
        try {
            return objectMapper.readValue(dbData, TypeRef)
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }
    }
}
