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
