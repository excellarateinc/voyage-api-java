package launchpad.security.user

import launchpad.security.role.RoleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import spock.lang.Specification

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationSpec extends Specification {
    private static final Long ROLE_STANDARD_ID = 3

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private RoleService roleService

    @Autowired
    private UserService userService

    def '/v1/users GET - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .getForEntity('/v1/users', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/v1/users GET - Super User access granted'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .withBasicAuth('super', 'password')
                            .getForEntity('/v1/users', Iterable)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.size() == 3
            responseEntity.body[0].id == 1L
            responseEntity.body[0].firstName == 'Super'
            responseEntity.body[0].lastName == 'User'
            responseEntity.body[0].username == 'super'
            responseEntity.body[0].email == 'support@LighthouseSoftware.com'
            responseEntity.body[0].password == '$2a$10$.Qa2l9VysOeG5M8HhgUbQ.h8KlTBLdMY/slPwMtL/I5OYibYUFQle'
            responseEntity.body[0].isEnabled
            !responseEntity.body[0].isAccountExpired
            !responseEntity.body[0].isCredentialsExpired
            !responseEntity.body[0].isAccountLocked
    }

    def '/v1/users GET - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .withBasicAuth('standard', 'password')
                            .getForEntity('/v1/users', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Access Denied'
    }

    def '/v1/users GET - Standard User with permission "api.users.list" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.users.list')

        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .withBasicAuth('standard', 'password')
                            .getForEntity('/v1/users', Iterable)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.size() == 3
            responseEntity.body[0].firstName == 'Super'
    }

    def '/v1/users POST - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .postForEntity('/v1/users', null, Iterable, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/v1/users POST - Super User access granted'() {
        given:
            User user = new User(firstName:'Test1', lastName:'User', username:'username', email:'test@test.com', password:'password')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<User> responseEntity =
                    restTemplate
                            .withBasicAuth('super', 'password')
                            .postForEntity('/v1/users', httpEntity, User)

        then:
            responseEntity.statusCode.value() == 201
            responseEntity.headers.getFirst('location') == '/v1/users/4'
            responseEntity.body.id
            responseEntity.body.firstName == 'Test1'
            responseEntity.body.lastName == 'User'
            responseEntity.body.username == 'username'
            responseEntity.body.email == 'test@test.com'
            responseEntity.body.password == 'password'
    }

    def '/v1/users POST - Standard User access denied'() {
        given:
            User user = new User(firstName:'Test2', lastName:'User', username:'username', email:'test@test.com', password:'password')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .withBasicAuth('standard', 'password')
                            .postForEntity('/v1/users', httpEntity, Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Access Denied'
    }

    def '/v1/users POST - Standard User with permission "api.users.create" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.users.create')

            User user = new User(firstName:'Test2', lastName:'User', username:'username', email:'test@test.com', password:'password')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<User> responseEntity =
                    restTemplate
                            .withBasicAuth('standard', 'password')
                            .postForEntity('/v1/users', httpEntity, User)

        then:
            responseEntity.statusCode.value() == 201
            responseEntity.headers.getFirst('location') == '/v1/users/5'
            responseEntity.body.firstName == 'Test2'
    }

    def '/v1/users/{id} GET - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .getForEntity('/v1/users/1', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/v1/users/{id} GET - Super User access granted'() {
        when:
            ResponseEntity<User> responseEntity =
                    restTemplate
                            .withBasicAuth('super', 'password')
                            .getForEntity('/v1/users/1', User)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == 1L
            responseEntity.body.firstName == 'Super'
            responseEntity.body.lastName == 'User'
            responseEntity.body.username == 'super'
            responseEntity.body.email == 'support@LighthouseSoftware.com'
            responseEntity.body.password == '$2a$10$.Qa2l9VysOeG5M8HhgUbQ.h8KlTBLdMY/slPwMtL/I5OYibYUFQle'
            responseEntity.body.isEnabled
            !responseEntity.body.isAccountExpired
            !responseEntity.body.isCredentialsExpired
            !responseEntity.body.isAccountLocked
    }

    def '/v1/users/{id} GET - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .withBasicAuth('standard', 'password')
                            .getForEntity('/v1/users/1', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Access Denied'
    }

    def '/v1/users/{id} GET - Standard User with permission "api.users.get" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.users.get')

        when:
            ResponseEntity<User> responseEntity =
                    restTemplate
                            .withBasicAuth('standard', 'password')
                            .getForEntity('/v1/users/1', User)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == 1L
            responseEntity.body.firstName == 'Super'
            responseEntity.body.lastName == 'User'
            responseEntity.body.username == 'super'
            responseEntity.body.email == 'support@LighthouseSoftware.com'
            responseEntity.body.password == '$2a$10$.Qa2l9VysOeG5M8HhgUbQ.h8KlTBLdMY/slPwMtL/I5OYibYUFQle'
            responseEntity.body.isEnabled
            !responseEntity.body.isAccountExpired
            !responseEntity.body.isCredentialsExpired
            !responseEntity.body.isAccountLocked
    }

    def '/v1/users/{id} GET - Invalid ID returns a 400 Bad Request response'() {
        when:
           ResponseEntity<Iterable> responseEntity =
                restTemplate
                        .withBasicAuth('super', 'password')
                        .getForEntity('/v1/users/999999', Iterable)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '400_bad_request'
            responseEntity.body[0].description == 'Unknown record identifier provided'
    }

    def '/v1/users/{id} PUT - Anonymous access denied'() {
        given:
            User user = new User(firstName:'Test3', lastName:'User', username:'username', email:'test@test.com', password:'password')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .exchange('/v1/users/1', HttpMethod.PUT, httpEntity, Iterable, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/v1/users/{id} PUT - Super User access granted'() {
        given:
            User user = new User(
                firstName:'Test3', lastName:'User', username:'username', email:'test@test.com', password:'password',
            )
            user = userService.save(user)

            user.firstName = 'Test3-UPDATED'

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<User> responseEntity =
                    restTemplate
                            .withBasicAuth('super', 'password')
                            .exchange('/v1/users/1', HttpMethod.PUT, httpEntity, User)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == user.id
            responseEntity.body.firstName == 'Test3-UPDATED'
            responseEntity.body.lastName == 'User'
            responseEntity.body.username == 'username'
            responseEntity.body.email == 'test@test.com'
            responseEntity.body.password == 'password'
    }

    def '/v1/users/{id} PUT - Standard User access denied'() {
        given:
            User user = new User(firstName:'Test4', lastName:'User', username:'username', email:'test@test.com', password:'password')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .withBasicAuth('standard', 'password')
                            .exchange('/v1/users/1', HttpMethod.PUT, httpEntity, Iterable, Collections.EMPTY_MAP)

        then:
        responseEntity.statusCode.value() == 401
        responseEntity.body.size() == 1
        responseEntity.body[0].code == '401_unauthorized'
        responseEntity.body[0].description == '401 Unauthorized. Access Denied'
    }

    def '/v1/users/{id} PUT - Standard User with permission "api.users.update" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.users.update')

            User user = new User(firstName:'Test4', lastName:'User', username:'username', email:'test@test.com', password:'password')
            user = userService.save(user)

            user.firstName = 'Test4-UPDATED'

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<User> responseEntity =
                    restTemplate
                            .withBasicAuth('standard', 'password')
                            .exchange('/v1/users/1', HttpMethod.PUT, httpEntity, User)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == user.id
            responseEntity.body.firstName == 'Test4-UPDATED'
            responseEntity.body.lastName == 'User'
            responseEntity.body.username == 'username'
            responseEntity.body.email == 'test@test.com'
            responseEntity.body.password == 'password'
    }

    def '/v1/users/{id} PUT - Invalid ID returns a 400 Bad Request response'() {
        given:
            User user = new User(id:9999, firstName:'Test4', lastName:'User', username:'username', email:'test@test.com', password:'password')

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<Iterable> responseEntity =
                restTemplate
                    .withBasicAuth('super', 'password')
                    .exchange('/v1/users/9999', HttpMethod.PUT, httpEntity, Iterable)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body[0].code == '400_bad_request'
            responseEntity.body[0].description == 'Unknown record identifier provided'
    }

    def '/v1/users/{id} DELETE - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .exchange('/v1/users/1', HttpMethod.DELETE, null, Iterable, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/v1/users/{id} DELETE - Super User access granted'() {
        given:
            User newUser = new User(firstName:'Test5', lastName:'User', username:'username', email:'test@test.com', password:'password')
            newUser = userService.save(newUser)

        when:
            ResponseEntity<String> responseEntity =
                restTemplate
                        .withBasicAuth('super', 'password')
                        .exchange("/v1/users/${newUser.id}", HttpMethod.DELETE, null, String, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null
    }

    def '/v1/users/{id} DELETE - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .withBasicAuth('standard', 'password')
                            .exchange('/v1/users/1', HttpMethod.DELETE, null, Iterable, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Access Denied'
    }

    def '/v1/users/{id} DELETE - Standard User with permission "api.users.delete" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.users.delete')

            User newUser = new User(firstName:'Test6', lastName:'User', username:'username', email:'test@test.com', password:'password')
            newUser = userService.save(newUser)

        when:
            ResponseEntity<String> responseEntity =
                    restTemplate
                            .withBasicAuth('standard', 'password')
                            .exchange("/v1/users/${newUser.id}", HttpMethod.DELETE, null, String, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null
    }

    def '/v1/users/{id} DELETE - Invalid ID returns a 400 Bad Request response'() {
        when:
           ResponseEntity<Iterable> responseEntity =
                restTemplate
                    .withBasicAuth('super', 'password')
                    .exchange('/v1/users/9999', HttpMethod.DELETE, null, Iterable, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '400_bad_request'
            responseEntity.body[0].description == 'Unknown record identifier provided'
    }
}
