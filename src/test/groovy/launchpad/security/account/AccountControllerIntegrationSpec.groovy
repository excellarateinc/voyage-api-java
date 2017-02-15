package launchpad.security.account

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import launchpad.account.VerifyMethod
import launchpad.account.VerifyType
import launchpad.security.user.User
import launchpad.security.user.UserService
import launchpad.test.AbstractIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

//TODO: Update tests to cover all the actions in the controller
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerIntegrationSpec extends AbstractIntegrationTest {

    private GreenMail greenMailSMTP

    @Autowired
    private UserService userService

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

    def '/api/v1/account/verify/methods GET - Anonymous access denied'() {
        given:
            User user = new User(firstName:'Test3', lastName:'User', username:'username3', email:'test@test.com', password:'password')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/account/verify/methods', httpEntity, Iterable)
        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/account/verify/methods GET - Standard User with permission "isAuthenticated()" access granted'() {
        given:
            User user = new User(firstName:'Test3', lastName:'User', username:'client-standard', email:'test@test.com', password:'password')
            userService.saveDetached(user)
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/account/verify/methods', Iterable, standardClient)
        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.size() == 1
            VerifyType."${responseEntity.body[0].verifyType}" == VerifyType.EMAIL
            responseEntity.body[0].label
            !responseEntity.body[0].value
    }

    def '/api/v1/account/verify/send POST - Anonymous access denied'() {
        given:
            User user = new User(firstName:'Test3', lastName:'User', username:'username3', email:'test@test.com', password:'password')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)
        when:
            ResponseEntity<Iterable> responseEntity = POST('/api/v1/account/verify/send', httpEntity, Iterable, standardClient)
        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null
    }

    def '/api/v1/account/verify/send POST - Standard User with permission "isAuthenticated()" access granted'() {
        given:
            User user = new User(firstName:'Test4', lastName:'User', username:'client-standard', email:'test@test.com', password:'password')
            userService.saveDetached(user)
            VerifyMethod verifyMethod = new VerifyMethod(verifyType: VerifyType.TEXT, value: '9999999999', label: 'phone')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<VerifyMethod> httpEntity = new HttpEntity<VerifyMethod>(verifyMethod, headers)
        when:
            ResponseEntity responseEntity = POST('/api/v1/account/verify/send', httpEntity, standardClient)
        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null
    }
}
