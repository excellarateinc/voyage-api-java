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
package voyage.security.crypto

import org.springframework.beans.factory.annotation.Autowired
import voyage.test.AbstractIntegrationTest

class CryptoServiceIntegrationSpec extends AbstractIntegrationTest {
    @Autowired
    CryptoService cryptoService

    def 'hash encode and decode text'() {
        given:
            String plaintext = 'this is plaintext'

        when:
            String encoded = cryptoService.hashEncode(plaintext)

        then:
            cryptoService.hashMatches(plaintext, encoded)
    }

    def 'hashMatches returns false if plaintext is empty'() {
        when:
            boolean isPlaintextNullMatches = cryptoService.hashMatches(null, 'text')
            boolean isPlaintextEmptyMatches = cryptoService.hashMatches('', 'text')

        then:
            !isPlaintextNullMatches
            !isPlaintextEmptyMatches
    }

    def 'hashMatches returns false if encoded value is empty'() {
        when:
            boolean isEncodedNullMatches = cryptoService.hashMatches('text', null)
            boolean isEncodedEmptyMatches = cryptoService.hashMatches('text', '')

        then:
            !isEncodedNullMatches
            !isEncodedEmptyMatches
    }

    def 'hashEncode returns null if plaintext value is null or empty'() {
        when:
            String encodedWithNull = cryptoService.hashEncode(null)
            String encodedWithEmpty = cryptoService.hashEncode('')

        then:
            !encodedWithNull
            !encodedWithEmpty
    }

    def 'encrypt and decrypt plaintext'() {
        given:
            String plaintext = 'this is plaintext'

        when:
            String encrypted = cryptoService.encrypt(plaintext)
            String decrypted = cryptoService.decrypt(encrypted)

        then:
            plaintext != encrypted
            plaintext == decrypted
    }
}
