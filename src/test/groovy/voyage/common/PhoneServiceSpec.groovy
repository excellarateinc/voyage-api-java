package voyage.common

import spock.lang.Specification

class PhoneServiceSpec extends Specification {
    private final PhoneService phoneService = new PhoneService()

    def 'toE164 parses US i18n phone number'() {
        when:
            String number = phoneService.toE164('+14155552671')
        then:
            number == '+14155552671'
    }

    def 'toE164 parses UK i18n phone number'() {
        when:
            String number = phoneService.toE164('+442071838750')
        then:
            number == '+442071838750'
    }

    def 'toE164 parses BR i18n phone number'() {
        when:
            String number = phoneService.toE164('+55-11-5525-6325')
        then:
            number == '+551155256325'
    }

    def 'toE164 parses US phone number and formats to US national'() {
        when:
            String number = phoneService.toE164('+14155552671')
        then:
            number == '+14155552671'
    }

    def 'toE164 parses US phone number without country code and throws exception'() {
        when:
            phoneService.toE164('4155552671')
        then:
            PhoneNumberInvalidException e = thrown()
            e.message == 'Phone number parse error for \'4155552671\': Missing or invalid default region.'
            e.errorCode == '400_phone_invalid_invalid_country_code'
    }

    def 'toE164 parses US phone number with too many digits throws exception'() {
        when:
           phoneService.toE164('+1452671778888')
        then:
            PhoneNumberInvalidException e = thrown()
            e.message == 'The phone number is not in the E164 format: +1452671778888'
            e.errorCode == '400_phone_invalid'
    }
}
