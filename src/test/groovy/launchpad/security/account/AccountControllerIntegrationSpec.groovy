package launchpad.security.account

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import launchpad.security.user.User
import launchpad.test.AbstractIntegrationTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

//TODO: Update tests to cover all the actions in the controller
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerIntegrationSpec extends AbstractIntegrationTest {

    private GreenMail greenMailSMTP

    def setup() {
        ServerSetup setup = new ServerSetup(3025, 'localhost', ServerSetup.PROTOCOL_SMTP)
        greenMailSMTP = new GreenMail(setup)
        greenMailSMTP.start()
    }

    def cleanup() {
        greenMailSMTP.stop()
    }

    def '/api/v1/account/register POST - Account create '() {
        given:
            User user = new User(firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<User> responseEntity = POST('/api/v1/account/register', httpEntity, User, standardClient)

        then:
            responseEntity.statusCode.value() == 201
            responseEntity.body.id
            responseEntity.body.firstName == 'Test1'
            responseEntity.body.lastName == 'User'
            responseEntity.body.username == 'username'
            responseEntity.body.email == 'test@test.com'
            responseEntity.body.password == 'password'
    }

    def '/api/v1/account/verify/initiate GET - Anonymous access denied'() {
        when:
        ResponseEntity<Iterable> responseEntity = GET('/api/v1/account/verify/initiate', Iterable)

        then:
        responseEntity.statusCode.value() == 401
        responseEntity.body.size() == 1
        responseEntity.body[0].error == '401_unauthorized'
        responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/account/verify/code GET - Anonymous access denied'() {
        when:
        ResponseEntity<Iterable> responseEntity = GET('/api/v1/account/verify/code', Iterable)

        then:
        responseEntity.statusCode.value() == 401
        responseEntity.body.size() == 1
        responseEntity.body[0].error == '401_unauthorized'
        responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/account/verify POST - Anonymous access denied'() {
        when:
        ResponseEntity<Iterable> responseEntity = POST('/api/v1/account/verify', Iterable)

        then:
        responseEntity.statusCode.value() == 401
        responseEntity.body.size() == 1
        responseEntity.body[0].error == '401_unauthorized'
        responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }
}