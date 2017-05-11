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

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyStore
import java.security.PublicKey
import java.security.interfaces.RSAPrivateCrtKey
import java.security.spec.RSAPublicKeySpec

@Service
class KeyStoreService {
    private static final String JKS_KEYSTORE = 'jks'
    private static final String ALGORITHM = 'RSA'
    private final Object lock = new Object()
    private final KeyStore keyStore

    @SuppressWarnings(['SpaceAfterOpeningBrace'])
    KeyStoreService(@Value('${security.key-store.filename}') String keyStoreFileName,
                    @Value('${security.key-store.password}') String keyStorePassword) {
        Resource keyStoreFile = new ClassPathResource(keyStoreFileName)
        char[] keyStoreFilePassword = keyStorePassword.toCharArray()
        synchronized (lock) {
            if (keyStore == null) {
                keyStore = KeyStore.getInstance(JKS_KEYSTORE)
                keyStore.load(keyStoreFile.inputStream, keyStoreFilePassword)
            }
        }
    }

    KeyPair getRsaKeyPair(String alias, char[] password) {
        if (keyStore) {
            RSAPrivateCrtKey key = (RSAPrivateCrtKey)keyStore.getKey(alias, password)
            RSAPublicKeySpec spec = new RSAPublicKeySpec(key.modulus, key.publicExponent)
            PublicKey publicKey = KeyFactory.getInstance(ALGORITHM).generatePublic(spec)
            return new KeyPair(publicKey, key)
        }
        throw new IllegalStateException('Error retrieving keys cryptographic services')
    }
}
