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

import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

import javax.crypto.Cipher
import java.security.KeyPair

@Service
class CryptoService {
    public static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder()
    private static final String ALGORITHM = 'RSA'
    private static final String ENCODING = 'UTF-8'
    private final Cipher cipher
    private final KeyPair keyPair

    @Autowired
    CryptoService(KeyStoreService keyStoreService,
                  @Value('${security.crypto.private-key-name}') String privateKeyName,
                  @Value('${security.crypto.private-key-password}') String privateKeyPassword) {
        this.cipher = Cipher.getInstance(ALGORITHM)
        this.keyPair = keyStoreService.getRsaKeyPair(privateKeyName, privateKeyPassword.toCharArray())
    }

    String encrypt(String plaintext) {
        this.cipher.init(Cipher.ENCRYPT_MODE, keyPair.private)
        return Base64.encodeBase64String(cipher.doFinal(plaintext.getBytes(ENCODING)))
    }

    String decrypt(String encryptedMsg) {
        this.cipher.init(Cipher.DECRYPT_MODE, keyPair.public)
        return new String(cipher.doFinal(Base64.decodeBase64(encryptedMsg)), ENCODING)
    }

    String hashEncode(String plaintext) {
        if (!plaintext) {
            return null
        }
        return PASSWORD_ENCODER.encode(plaintext)
    }

    boolean hashMatches(String plaintext, String hashValue) {
        if (!plaintext || !hashValue) {
            return false
        }
        return PASSWORD_ENCODER.matches(plaintext, hashValue)
    }
}
