package launchpad.security

import launchpad.security.permission.Permission
import launchpad.security.permission.PermissionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.ResponseEntity
import spock.lang.Specification

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityIntegrationSpec extends Specification {
    private static final Long SUPER_USER_ID = 1L

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private PermissionService permissionService

    def 'Authorization required to access the base "/" URL (covering all API access)'() {
        when:
            ResponseEntity<Iterable> responseEntity = restTemplate.getForEntity('/', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
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

    def 'Super User can access the base "/" URL but receives a 404 because that resource does not exist'() {
        when:
        ResponseEntity<Iterable> responseEntity = restTemplate.withBasicAuth('super', 'password').getForEntity('/', Iterable)

        then:
        responseEntity.statusCode.value() == 404
        responseEntity.body.size() == 1
        responseEntity.body[0].code == '404_not_found'
    }

    def 'Anonymous user is denied when accessing secured web service "/v1/users" GET'() {
        when:
            ResponseEntity<Iterable> responseEntity = restTemplate.getForEntity('/v1/users', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def 'Super User login is denied after providing wrong password'() {
        when:
            ResponseEntity<Iterable> responseEntity = restTemplate.withBasicAuth('super', 'wrong password').getForEntity('/v1/users', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Bad credentials'
    }

    def 'Super User login is accepted after providing correct password'() {
        when:
            ResponseEntity<Iterable> responseEntity = restTemplate.withBasicAuth('super', 'password').getForEntity('/v1/users', Iterable)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body[0].id == 1
            responseEntity.body[0].firstName == 'Super'
            responseEntity.body[0].lastName == 'User'
    }
}
