package voyage.security.verify

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import voyage.test.AbstractIntegrationTest

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class VerifyControllerIntegrationSpec extends AbstractIntegrationTest {

    private GreenMail greenMailSMTP

    def setup() {
        ServerSetup setup = new ServerSetup(3025, 'localhost', ServerSetup.PROTOCOL_SMTP)
        greenMailSMTP = new GreenMail(setup)
        greenMailSMTP.start()
    }

    def cleanup() {
        greenMailSMTP.stop()
    }

    /*
       Run the /verify POST test before the /verify/send because the /send will reset the 'code' with a new value. Since
       the /verify process sends the code to a mobile number, there is no easy way to intercept that code value from an
       integration test. 
     */
    def '/api/v1/verify POST - Standard User with permission "isAuthenticated()" access granted'() {
        given:
            String body = '{"code":"code"}'
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers)
        when:
            ResponseEntity responseEntity = POST('/api/v1/verify', httpEntity, String, superClient)
        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null
    }
    
    def '/api/v1/verify POST - Anonymous access denied'() {
        given:
            String body = '{"code":"code"}'
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers)
        when:
            ResponseEntity responseEntity = POST('/api/v1/verify', httpEntity, String)
        then:
            responseEntity.statusCode.value() == 401
    }

    def '/api/v1/verify/send POST - Anonymous access denied'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)
        when:
            ResponseEntity<String> responseEntity = POST('/api/v1/verify/send', httpEntity, String)
        then:
            responseEntity.statusCode.value() == 401
    }

    def '/api/v1/verify/send POST - Standard User with permission "isAuthenticated()" access granted'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)
        when:
            ResponseEntity responseEntity = GET('/api/v1/verify/send', httpEntity, String, superClient)
        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null
    }
}
