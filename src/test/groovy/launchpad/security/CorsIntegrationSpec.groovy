package launchpad.security

import launchpad.security.user.User
import launchpad.test.AbstractIntegrationTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

class CorsIntegrationSpec extends AbstractIntegrationTest {

    def 'Anonymous GET request with Origin header to public /api/hello returns public CORS response headers'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setOrigin('http://attacker.com/')
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)

        when:
            ResponseEntity<String> responseEntity = GET('/api/hello', httpEntity, String)

        then:
            responseEntity.statusCode.value() == 200
            !responseEntity.headers.getFirst('Vary')
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == '*'
            !responseEntity.headers.getFirst('Access-Control-Allow-Credentials')
    }

    def 'Super User GET request with Origin header to public /api/hello returns valid CORS response headers'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setOrigin('http://attacker.com/')
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)

        when:
            ResponseEntity<String> responseEntity = GET('/api/hello', httpEntity, String, superClient)

        then:
            responseEntity.statusCode.value() == 200
            !responseEntity.headers.getFirst('Vary')
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == '*'
            !responseEntity.headers.getFirst('Access-Control-Allow-Credentials')
    }

    def 'Anonymous OPTIONS request with Origin header to public /api/hello returns valid CORS response headers'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setOrigin('http://test.com/')
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)

        when:
            ResponseEntity<String> responseEntity = OPTIONS('/api/hello', httpEntity, String)

        then:
            responseEntity.statusCode.value() == 200
            !responseEntity.headers.getFirst('Vary')
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == '*'
            !responseEntity.headers.getFirst('Access-Control-Allow-Credentials')
    }

    def 'Super User OPTIONS request with Origin header to public /api/hello returns valid CORS response headers'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setOrigin('http://test.com/')
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)

        when:
            ResponseEntity<String> responseEntity = OPTIONS('/api/hello', httpEntity, String, superClient)

        then:
            responseEntity.statusCode.value() == 200
            !responseEntity.headers.getFirst('Vary')
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == '*'
            !responseEntity.headers.getFirst('Access-Control-Allow-Credentials')
    }

    def 'Anonymous OPTIONS request with Origin header to protected /api/v1/users returns valid CORS response headers'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setOrigin('http://test.com/')
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)

        when:
            ResponseEntity<String> responseEntity = OPTIONS('/api/v1/users', httpEntity, String)

        then:
            responseEntity.statusCode.value() == 200
            !responseEntity.headers.getFirst('Vary')
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == '*'
            !responseEntity.headers.getFirst('Access-Control-Allow-Credentials')
    }

    def 'Super User OPTIONS request with Origin header to protected /api/v1/users returns valid CORS response headers'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setOrigin('http://localhost/')
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)

        when:
            ResponseEntity<String> responseEntity = OPTIONS('/api/v1/users', httpEntity, String, superClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.headers.getFirst('Vary') == 'Origin'
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == 'http://localhost'
            responseEntity.headers.getFirst('Access-Control-Allow-Credentials') == 'true'
    }

    def 'Anonymous GET request with Origin header to protected /api/v1/users returns a 401 Unauthorized'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setOrigin('http://attacker.com/')
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)

        when:
            ResponseEntity<String> responseEntity = GET('/api/v1/users', httpEntity, String)

        then:
            responseEntity.statusCode.value() == 401
            !responseEntity.headers.getFirst('Vary')
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == '*'
            !responseEntity.headers.getFirst('Access-Control-Allow-Credentials')
    }

    def 'Super User GET request with Origin header to protected /api/v1/users returns valid CORS response headers'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setOrigin('http://test.com/')
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)

        when:
            ResponseEntity<String> responseEntity = GET('/api/v1/users', httpEntity, String, superClient)

        then:
            responseEntity.statusCode.value() == 200
            !responseEntity.headers.getFirst('Vary')
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == '*'
            !responseEntity.headers.getFirst('Access-Control-Allow-Credentials')
    }

    def 'Anonymous POST request with Origin header to protected /api/v1/users returns a 401 Unauthorized'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setOrigin('http://attacker.com/')
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)

        when:
            ResponseEntity<String> responseEntity = POST('/api/v1/users', httpEntity, String)

        then:
            responseEntity.statusCode.value() == 401
            !responseEntity.headers.getFirst('Vary')
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == '*'
            !responseEntity.headers.getFirst('Access-Control-Allow-Credentials')
    }

    def 'Super User POST request with Origin header to protected /api/v1/users returns valid CORS response headers'() {
        given:
            User user = new User(firstName:'TestCORS', lastName:'User', username:'CORS', email:'CORS@email.com', password:'password')
            HttpHeaders headers = new HttpHeaders()
            headers.setOrigin('http://localhost/')
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<User> responseEntity = POST('/api/v1/users', httpEntity, User, superClient)

        then:
            responseEntity.statusCode.value() == 201
            responseEntity.headers.getFirst('Vary') == 'Origin'
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == 'http://localhost'
            responseEntity.headers.getFirst('Access-Control-Allow-Credentials') == 'true'
    }

    def 'Anonymous PUT request with Origin header to protected /api/v1/users returns a 401 Unauthorized'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setOrigin('http://attacker.com/')
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)

        when:
           ResponseEntity<String> responseEntity = PUT('/api/v1/users', httpEntity, String)

        then:
            responseEntity.statusCode.value() == 401
            !responseEntity.headers.getFirst('Vary')
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == '*'
            !responseEntity.headers.getFirst('Access-Control-Allow-Credentials')
    }

    def 'Anonymous DELETE request with Origin header to protected /api/v1/users returns a 401 Unauthorized'() {
        given:
            HttpHeaders headers = new HttpHeaders()
            headers.setOrigin('http://attacker.com/')
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers)

        when:
           ResponseEntity<String> responseEntity = DELETE('/api/v1/users', httpEntity, String)

        then:
            responseEntity.statusCode.value() == 401
            !responseEntity.headers.getFirst('Vary')
            responseEntity.headers.getFirst('Access-Control-Allow-Origin') == '*'
            !responseEntity.headers.getFirst('Access-Control-Allow-Credentials')
    }
}
