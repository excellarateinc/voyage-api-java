package launchpad.security

import launchpad.security.permission.Permission
import launchpad.security.permission.PermissionService
import launchpad.test.AbstractIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity

class SecurityIntegrationSpec extends AbstractIntegrationTest {
    private static final Long SUPER_USER_ID = 1L

    @Autowired
    private PermissionService permissionService

    def 'Authorization required to access the base "/" URL (covering all app access)'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def 'Anonymous access to "/resources/css" is valid'() {
        when:
            ResponseEntity<String> responseEntity = GET('/resources/css/common.css', String)

        then:
            responseEntity.statusCode.value() == 200
    }

    def 'Anonymous access to "/webjars/**" is valid'() {
        when:
            ResponseEntity<String> responseEntity = GET('/webjars/bootstrap/css/bootstrap.min.css', String)

        then:
            responseEntity.statusCode.value() == 200
    }

    def 'Anonymous access to "/oauth/token" is denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/oauth/token', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def 'Anonymous access to "/oauth/authorize" is redirected to the /login page'() {
        when:
            ResponseEntity<String> responseEntity = GET('/oauth/authorize', String)

        then:
            responseEntity.statusCode.value() == 302
            responseEntity.headers.getFirst('Location').indexOf('/login') > 0
    }

    def 'Anonymous access to "/api/hello" is permitted'() {
        when:
            ResponseEntity<String> responseEntity = GET('/api/hello', String)

        then:
            responseEntity.statusCode.value() == 200
    }

    def 'Anonymous access to "/api/v1/users" is denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/users', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def 'Super User has every permission in the database'() {
        when:
            Iterable<Permission> permissions = permissionService.listAll()
            Iterable<Permission> superPermissions = permissionService.findAllByUser(SUPER_USER_ID)

        then:
            superPermissions.size() == permissions.size()

            Iterable<Permission> commonPermissions = permissions.intersect(superPermissions)
            commonPermissions.size() == permissions.size()
    }

    def 'Super User (not client) can access the base "/" URL via BasicAuth but receives a 404 because that resource does not exist'() {
        when:
            ResponseEntity<String> responseEntity = restTemplate.withBasicAuth('super', 'password').getForEntity('/', String)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.indexOf('I think you\'re lost!') > 0
    }

    def 'Anonymous user is denied when accessing secured web service "/v1/users" GET'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/users', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def 'Super User login cannot access /api resource server via BasicAuth'() {
        when:
            ResponseEntity<Iterable> responseEntity = restTemplate.withBasicAuth('super', 'password').getForEntity('/api/v1/users', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def 'Super User login is accepted after providing correct password'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/users', Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body[0].id == 1
            responseEntity.body[0].firstName == 'Super'
            responseEntity.body[0].lastName == 'User'
    }
}
