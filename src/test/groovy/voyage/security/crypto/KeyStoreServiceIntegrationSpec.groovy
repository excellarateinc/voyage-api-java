package voyage.security.crypto

import org.springframework.beans.factory.annotation.Autowired
import voyage.test.AbstractIntegrationTest

import java.security.KeyPair

class KeyStoreServiceIntegrationSpec extends AbstractIntegrationTest {
    @Autowired
    KeyStoreService keyStoreService

    def 'getRsaKeyPair returns a KeyPair from the keystore'() {
        when:
            KeyPair keyPair = keyStoreService.getRsaKeyPair('asymmetric', 'changeme'.toCharArray())

        then:
            keyPair.public
            keyPair.private
    }

    def 'KeyStoreService throws exception when the keystore cannot be loaded'() {
        when:
            KeyStoreService service = new KeyStoreService('keystore.jks', 'wrong-password')

        then:
            !service
            IOException ex = thrown()
            ex != null
    }
}
