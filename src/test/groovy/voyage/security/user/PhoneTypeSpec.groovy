/*
 * Copyright 2018 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
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
package voyage.security.user

import spock.lang.Specification

class PhoneTypeSpec extends Specification {

    def 'ofValue - converts a mixed case enum value'() {
        when:
            PhoneType phoneType = PhoneType.fromValue('mObilE')
        then:
            phoneType == PhoneType.MOBILE
    }

    def 'ofValue - converts a lower case enum value'() {
        when:
           PhoneType phoneType = PhoneType.fromValue('mobile')
        then:
            phoneType == PhoneType.MOBILE
    }

    def 'ofValue - converts an upper case enum value'() {
        when:
           PhoneType phoneType = PhoneType.fromValue('MOBILE')
        then:
            phoneType == PhoneType.MOBILE
    }

    def 'ofValue - Returns OTHER if the value is not recognized'() {
        when:
           PhoneType phoneType = PhoneType.fromValue('BLAH')
        then:
            phoneType == PhoneType.OTHER
    }
}