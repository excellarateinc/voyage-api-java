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
    public static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder()
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
        return passwordEncoder.encode(plaintext)
    }

    boolean hashMatches(String plaintext, String hashValue) {
        if (!plaintext || !hashValue) {
            return false
        }
        return passwordEncoder.matches(plaintext, hashValue)
    }
}
