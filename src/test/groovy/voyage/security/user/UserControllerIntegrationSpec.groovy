package voyage.security.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import voyage.security.crypto.CryptoService
import voyage.security.role.RoleService
import voyage.test.AbstractIntegrationTest

class UserControllerIntegrationSpec extends AbstractIntegrationTest {
    private static final Long ROLE_STANDARD_ID = 2

    @Autowired
    private RoleService roleService

    @Autowired
    private UserService userService

    @Autowired CryptoService cryptoService

    def '/api/v1/users GET - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/users', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/users GET - Super User access granted'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/users', Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 200
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

    def '/api/v1/users GET - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/users', Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def '/api/v1/users GET - Standard User with permission "api.users.list" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.users.list')

        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/users', Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body[0].firstName == 'Super'
    }

    def '/api/v1/users POST - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = POST('/api/v1/users', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/users POST - Super User access granted'() {
        given:
            User user = new User(firstName:'Test1', lastName:'User', username:'username1', email:'test@test.com', password:'Nokia@5610')
            user.phones = [new UserPhone(phoneNumber:'+1-651-888-6021', phoneType:PhoneType.MOBILE)]
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<User> responseEntity = POST('/api/v1/users', httpEntity, User, superClient)

        then:
            responseEntity.statusCode.value() == 201
            responseEntity.body.id
            responseEntity.body.firstName == 'Test1'
            responseEntity.body.lastName == 'User'
            responseEntity.body.username == 'username1'
            responseEntity.body.email == 'test@test.com'
            cryptoService.hashMatches('Nokia@5610', responseEntity.body.password)
    }

    def '/api/v1/users POST - Standard User access denied'() {
        given:
            User user = new User(firstName:'Test2', lastName:'User', username:'username2', email:'test@test.com', password:'password')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<Iterable> responseEntity = POST('/api/v1/users', httpEntity, Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def '/api/v1/users POST - Standard User with permission "api.users.create" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.users.create')

            User user = new User(firstName:'Test2', lastName:'User', username:'username2', email:'test@test.com', password:'Test@1234')
            user.phones = [new UserPhone(phoneNumber:'+1-651-888-6021', phoneType:PhoneType.MOBILE)]
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<User> responseEntity = POST('/api/v1/users', httpEntity, User, standardClient)

        then:
            responseEntity.statusCode.value() == 201
            responseEntity.body.firstName == 'Test2'
    }

    def '/api/v1/users/{id} GET - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/users/1', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/users/{id} GET - Super User access granted'() {
        when:
            ResponseEntity<User> responseEntity = GET('/api/v1/users/1', User, superClient)

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

    def '/api/v1/users/{id} GET - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/users/1', Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def '/api/v1/users/{id} GET - Standard User with permission "api.users.get" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.users.get')

        when:
            ResponseEntity<User> responseEntity = GET('/api/v1/users/1', User, standardClient)

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

    def '/api/v1/users/{id} GET - Invalid ID returns a 404 Not Found response'() {
        when:
           ResponseEntity<Iterable> responseEntity = GET('/api/v1/users/999999', Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 404
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '404_unknown_identifier'
            responseEntity.body[0].errorDescription == 'Unknown record identifier provided'
    }

    def '/api/v1/users/{id} PUT - Anonymous access denied'() {
        given:
            User user = new User(firstName:'Test3', lastName:'User', username:'username3', email:'test@test.com', password:'password')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<Iterable> responseEntity = PUT('/api/v1/users/1', httpEntity, Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/users/{id} PUT - Super User access granted'() {
        given:
            User user = new User(
                firstName:'Test3', lastName:'User', username:'username4', email:'test@test.com', password:'Test@1234',
            )
            user.phones = [new UserPhone(phoneNumber:'+1-651-888-6021', phoneType:PhoneType.MOBILE)]
            user = userService.saveDetached(user)

            user.firstName = 'Test3-UPDATED'

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<User> responseEntity = PUT('/api/v1/users/1', httpEntity, User, superClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == user.id
            responseEntity.body.firstName == 'Test3-UPDATED'
            responseEntity.body.lastName == 'User'
            responseEntity.body.username == 'username4'
            responseEntity.body.email == 'test@test.com'
            cryptoService.hashMatches('Test@1234', responseEntity.body.password)
    }

    def '/api/v1/users/{id} PUT - Standard User access denied'() {
        given:
            User user = new User(firstName:'Test4', lastName:'User', username:'username5', email:'test@test.com', password:'Test@1234')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<Iterable> responseEntity = PUT('/api/v1/users/1', httpEntity, Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def '/api/v1/users/{id} PUT - Standard User with permission "api.users.update" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.users.update')

            User user = new User(firstName:'Test4', lastName:'User', username:'username6', email:'test@test.com', password:'Test@1234')
            user.phones = [new UserPhone(phoneNumber:'+1-651-888-6021', phoneType:PhoneType.MOBILE)]
            user = userService.saveDetached(user)

            user.firstName = 'Test4-UPDATED'

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<User> responseEntity = PUT('/api/v1/users/1', httpEntity, User, standardClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == user.id
            responseEntity.body.firstName == 'Test4-UPDATED'
            responseEntity.body.lastName == 'User'
            responseEntity.body.username == 'username6'
            responseEntity.body.email == 'test@test.com'
            cryptoService.hashMatches('Test@1234', responseEntity.body.password)
    }

    def '/api/v1/users/{id} PUT - Invalid ID returns a 404 Not Found response'() {
        given:
            User user = new User(id:9999, firstName:'Test4', lastName:'User', username:'username7', email:'test@test.com', password:'password')

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<User> httpEntity = new HttpEntity<User>(user, headers)

        when:
            ResponseEntity<Iterable> responseEntity = PUT('/api/v1/users/999999', httpEntity, Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 404
            responseEntity.body[0].error == '404_unknown_identifier'
            responseEntity.body[0].errorDescription == 'Unknown record identifier provided'
    }

    def '/api/v1/users/{id} DELETE - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = DELETE('/api/v1/users/1', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/users/{id} DELETE - Super User access granted'() {
        given:
            User newUser = new User(firstName:'Test5', lastName:'User', username:'username8', email:'test@test.com', password:'Test@1234')
            newUser.phones = [new UserPhone(phoneNumber:'+1-651-888-6021', phoneType:PhoneType.MOBILE)]
            newUser = userService.saveDetached(newUser)

        when:
            ResponseEntity<String> responseEntity = DELETE("/api/v1/users/${newUser.id}", String, superClient)

        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null
    }

    def '/api/v1/users/{id} DELETE - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = DELETE('/api/v1/users/1', Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def '/api/v1/users/{id} DELETE - Standard User with permission "api.users.delete" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.users.delete')

            User newUser = new User(firstName:'Test6', lastName:'User', username:'username9', email:'test@test.com', password:'Test@1234')
            newUser.phones = [new UserPhone(phoneNumber:'+1-651-888-6021', phoneType:PhoneType.MOBILE)]
            newUser = userService.saveDetached(newUser)

        when:
            ResponseEntity<String> responseEntity = DELETE("/api/v1/users/${newUser.id}", String, standardClient)

        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null
    }

    def '/api/v1/users/{id} DELETE - Invalid ID returns a 404 Not Found response'() {
        when:
           ResponseEntity<Iterable> responseEntity = DELETE('/api/v1/users/999999', Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 404
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '404_unknown_identifier'
            responseEntity.body[0].errorDescription == 'Unknown record identifier provided'
    }
}
