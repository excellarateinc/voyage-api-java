package voyage.security.verify

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import voyage.security.user.User
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

    def '/api/v1/verify/methods GET - Anonymous access denied'() {
        given:
            User user = new User(firstName:'Test3', lastName:'User', username:'username3', email:'test@test.com', password:'password')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/verify/methods', httpEntity, Iterable)
        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/verify/methods GET - Standard User with permission "isAuthenticated()" access granted'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/verify/methods', Iterable, standardClient)
        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.size() == 1
            VerifyType."${responseEntity.body[0].verifyType}" == VerifyType.EMAIL
            responseEntity.body[0].label
            !responseEntity.body[0].value
    }

    def '/api/v1/verify/send POST - Anonymous access denied'() {
        given:
            VerifyMethod verifyMethod = new VerifyMethod(verifyType:VerifyType.EMAIL, value:'', label:'email')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<VerifyMethod> httpEntity = new HttpEntity<VerifyMethod>(verifyMethod, headers)
        when:
            ResponseEntity<String> responseEntity = POST('/api/v1/verify/send', httpEntity, String)
        then:
            responseEntity.statusCode.value() == 401
    }

    def '/api/v1/verify/send POST - Standard User with permission "isAuthenticated()" access granted'() {
        given:
            VerifyMethod verifyMethod = new VerifyMethod(verifyType:VerifyType.EMAIL, value:'', label:'email')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<VerifyMethod> httpEntity = new HttpEntity<VerifyMethod>(verifyMethod, headers)
        when:
            ResponseEntity responseEntity = POST('/api/v1/verify/send', httpEntity, String, superClient)
        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null
    }

    def '/api/v1/verify POST - Anonymous access denied'() {
        given:
            String code = 'code'
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<String> httpEntity = new HttpEntity<String>(code, headers)
        when:
            ResponseEntity<String> responseEntity = POST('/api/v1/verify', httpEntity, String)
        then:
            responseEntity.statusCode.value() == 401
    }

    def '/api/v1/verify POST - Standard User with permission "isAuthenticated()" access granted'() {
        given:
            String code = 'code'
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<String> httpEntity = new HttpEntity<String>(code, headers)
        when:
            ResponseEntity responseEntity = POST('/api/v1/verify', httpEntity, String, superClient)
        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null
    }
}
