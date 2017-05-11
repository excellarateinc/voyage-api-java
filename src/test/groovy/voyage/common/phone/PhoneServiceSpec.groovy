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
