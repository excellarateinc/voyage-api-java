package voyage.connectedhealth.reminder

import spock.lang.Specification

class DayOfWeekListAttributeConverterSpec extends Specification {
    private DayOfWeekListAttributeConverter dayOfWeekListAttributeConverter = new DayOfWeekListAttributeConverter()

    def "convertToDatabaseColumn - convert json string to List<DayOfWeek>"() {
        given:
        def daysOfWeek = "[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]"

        when:
        def object = dayOfWeekListAttributeConverter.convertToEntityAttribute(daysOfWeek)

        then:
        object == [DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                   DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY]
    }

    def "convertToEntityAttribute - convert List<DayOfWeek> to json string"() {
        given:
        def daysOfWeek = [DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                          DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY]

        when:
        def object = dayOfWeekListAttributeConverter.convertToDatabaseColumn(daysOfWeek)

        then:
        object == "[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\",\"SATURDAY\",\"SUNDAY\"]"
    }
}
