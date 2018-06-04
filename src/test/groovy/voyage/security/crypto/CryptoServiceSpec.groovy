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

import spock.lang.Specification

class CryptoServiceSpec extends Specification {
    KeyStoreService keyStoreService = Mock()
    CryptoService cryptoService = new CryptoService(keyStoreService, 'test', 'test')

    def 'generate a secure random number'() {
        when:
            String random1 = cryptoService.secureRandomToken()
            String random2 = cryptoService.secureRandomToken()
            String random3 = cryptoService.secureRandomToken()
        then:
            random1.length() > 100
            random2.length() > 100
            random3.length() > 100
            random1 != random2
            random2 != random3
    }
}
