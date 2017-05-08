package voyage.common.phone

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class PhoneService {
    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.instance

    @Value('${app.default-country}')
    private String defaultCountry

    String toE164(String phoneNumberRaw) {
        return parseAndFormat(PhoneNumberFormat.E164, phoneNumberRaw)
    }

    private String parseAndFormat(PhoneNumberFormat format, String phoneNumberRaw) {
        if (!phoneNumberRaw) {
            return null
        }

        try {
            PhoneNumber phoneNumber = phoneNumberUtil.parse(phoneNumberRaw, defaultCountry)

            if (phoneNumberUtil.isPossibleNumber(phoneNumber) && phoneNumberUtil.isValidNumber(phoneNumber)) {
                return phoneNumberUtil.format(phoneNumber, format)
            }

            throw new PhoneNumberInvalidException("The phone number is not in the E164 format: ${phoneNumberRaw}")

        } catch (NumberParseException e) {
            throw new PhoneNumberInvalidException(
                    "Phone number parse error for '${phoneNumberRaw}': ${e.message}",
                    e.errorType.name().toLowerCase()
            )
        }
    }
}
