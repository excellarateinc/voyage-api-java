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
