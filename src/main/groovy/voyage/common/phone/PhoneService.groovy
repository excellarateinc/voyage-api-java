/*
 * Copyright 2017 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
