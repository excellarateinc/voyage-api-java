package launchpad.security.role

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.annotation.Rollback
import spock.lang.Specification

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoleControllerIntegrationSpec extends Specification {
    private static final Long ROLE_STANDARD_ID = 3

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private RoleService roleService

    def '/v1/roles GET - Anonymous access denied'() {
        when:
        ResponseEntity<Iterable> responseEntity =
                restTemplate
                        .getForEntity('/v1/roles', Iterable)

        then:
        responseEntity.statusCode.value() == 401
        responseEntity.body.size() == 1
        responseEntity.body[0].code == '401_unauthorized'
        responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/v1/roles GET - Super User access granted'() {
        when:
        ResponseEntity<Iterable> responseEntity =
                restTemplate
                        .withBasicAuth('super', 'password')
                        .getForEntity('/v1/roles', Iterable)

        then:
        responseEntity.statusCode.value() == 200
        responseEntity.body.size() == 3
        responseEntity.body[0].id == 1L
        responseEntity.body[0].name == 'Super User'
        responseEntity.body[0].authority == 'role.super'
    }

    def '/v1/roles GET - Standard User access denied'() {
        when:
        ResponseEntity<Iterable> responseEntity =
                restTemplate
                        .withBasicAuth('standard', 'password')
                        .getForEntity('/v1/roles', Iterable)

        then:
        responseEntity.statusCode.value() == 401
        responseEntity.body.size() == 1
        responseEntity.body[0].code == '401_unauthorized'
        responseEntity.body[0].description == '401 Unauthorized. Access Denied'
    }

    @Rollback
    def '/v1/roles GET - Standard User with permission "api.roles.list" access granted'() {
        given:
        roleService.addPermission(ROLE_STANDARD_ID, 'api.roles.list')

        when:
        ResponseEntity<Iterable> responseEntity =
                restTemplate
                        .withBasicAuth('standard', 'password')
                        .getForEntity('/v1/roles', Iterable)

        then:
        responseEntity.statusCode.value() == 200
        responseEntity.body.size() == 3
        responseEntity.body[0].id == 1L
        responseEntity.body[0].name == 'Super User'
        responseEntity.body[0].authority == 'role.super'
    }

    def '/v1/roles POST - Anonymous access denied'() {
        when:
        ResponseEntity<Iterable> responseEntity =
                restTemplate
                        .postForEntity('/v1/roles', null, Iterable, Collections.EMPTY_MAP)

        then:
        responseEntity.statusCode.value() == 401
        responseEntity.body.size() == 1
        responseEntity.body[0].code == '401_unauthorized'
        responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/v1/roles POST - Super User access granted'() {
        given:
        Role role = new Role(name:'Role-Name-1', authority:'test.role.authority.1')
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
        ResponseEntity<Role> responseEntity =
                restTemplate
                        .withBasicAuth('super', 'password')
                        .postForEntity('/v1/roles', httpEntity, Role)

        then:
        responseEntity.statusCode.value() == 201
        responseEntity.headers.getFirst('location') == '/v1/roles/4'
        responseEntity.body.id
        responseEntity.body.name == 'Role-Name-1'
        responseEntity.body.authority == 'test.role.authority.1'
    }

    def '/v1/roles POST - Standard User access denied'() {
        given:
        Role role = new Role(name:'Role-Name-2', authority:'test.role.authority.2')
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
        ResponseEntity<Iterable> responseEntity =
                restTemplate
                        .withBasicAuth('standard', 'password')
                        .postForEntity('/v1/roles', httpEntity, Iterable)

        then:
        responseEntity.statusCode.value() == 401
        responseEntity.body.size() == 1
        responseEntity.body[0].code == '401_unauthorized'
        responseEntity.body[0].description == '401 Unauthorized. Access Denied'
    }

    @Rollback
    def '/v1/roles POST - Standard User with permission "api.roles.create" access granted'() {
        given:
        roleService.addPermission(ROLE_STANDARD_ID, 'api.roles.create')

        Role role = new Role(name:'Role-Name-3', authority:'test.role.authority.3')
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
        ResponseEntity<Role> responseEntity =
                restTemplate
                        .withBasicAuth('standard', 'password')
                        .postForEntity('/v1/roles', httpEntity, Role)

        then:
        responseEntity.statusCode.value() == 201
        responseEntity.headers.getFirst('location') == '/v1/roles/5'
        responseEntity.body.id
        responseEntity.body.name == 'Role-Name-3'
        responseEntity.body.authority == 'test.role.authority.3'
    }

    def '/v1/roles/{id} GET - Anonymous access denied'() {
        when:
        ResponseEntity<Iterable> responseEntity =
                restTemplate
                        .getForEntity('/v1/roles/1', Iterable)

        then:
        responseEntity.statusCode.value() == 401
        responseEntity.body.size() == 1
        responseEntity.body[0].code == '401_unauthorized'
        responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/v1/roles/{id} GET - Super User access granted'() {
        when:
        ResponseEntity<Role> responseEntity =
                restTemplate
                        .withBasicAuth('super', 'password')
                        .getForEntity('/v1/roles/1', Role)

        then:
        responseEntity.statusCode.value() == 200
        responseEntity.body.id == 1L
        responseEntity.body.name == 'Super User'
        responseEntity.body.authority == 'role.super'
    }

    def '/v1/roles/{id} GET - Standard User access denied'() {
        when:
        ResponseEntity<Iterable> responseEntity =
                restTemplate
                        .withBasicAuth('standard', 'password')
                        .getForEntity('/v1/roles/1', Iterable)

        then:
        responseEntity.statusCode.value() == 401
        responseEntity.body.size() == 1
        responseEntity.body[0].code == '401_unauthorized'
        responseEntity.body[0].description == '401 Unauthorized. Access Denied'
    }

    @Rollback
    def '/v1/roles/{id} GET - Standard User with permission "api.roles.get" access granted'() {
        given:
        roleService.addPermission(ROLE_STANDARD_ID, 'api.roles.get')

        when:
        ResponseEntity<Role> responseEntity =
                restTemplate
                        .withBasicAuth('standard', 'password')
                        .getForEntity('/v1/roles/1', Role)

        then:
        responseEntity.statusCode.value() == 200
        responseEntity.body.id == 1L
        responseEntity.body.name == 'Super User'
        responseEntity.body.authority == 'role.super'
    }

    def '/v1/roles/{id} PUT - Anonymous access denied'() {
        given:
        Role role = new Role(name:'Role-Name-3', authority:'test.role.authority.3')
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
        ResponseEntity<Iterable> responseEntity =
                restTemplate
                        .exchange('/v1/roles/1', HttpMethod.PUT, httpEntity, Iterable, Collections.EMPTY_MAP)

        then:
        responseEntity.statusCode.value() == 401
        responseEntity.body.size() == 1
        responseEntity.body[0].code == '401_unauthorized'
        responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    @Rollback
    def '/v1/roles/{id} PUT - Super User access granted'() {
        given:
        Role role = new Role(name:'Role-Name-3', authority:'test.role.authority.3')
        role = roleService.save(role)

        role.name = 'Role-Name-3-Updated'

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
        ResponseEntity<Role> responseEntity =
                restTemplate
                        .withBasicAuth('super', 'password')
                        .exchange('/v1/roles/1', HttpMethod.PUT, httpEntity, Role)

        then:
        responseEntity.statusCode.value() == 200
        responseEntity.body.id == role.id
        responseEntity.body.name == 'Role-Name-3-Updated'
        responseEntity.body.authority == 'test.role.authority.3'
    }

    def '/v1/roles/{id} PUT - Standard User access denied'() {
        given:
        Role role = new Role(name:'Role-Name-4', authority:'test.role.authority.4')
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
        ResponseEntity<Iterable> responseEntity =
                restTemplate
                        .withBasicAuth('standard', 'password')
                        .exchange('/v1/roles/1', HttpMethod.PUT, httpEntity, Iterable, Collections.EMPTY_MAP)

        then:
        responseEntity.statusCode.value() == 401
        responseEntity.body.size() == 1
        responseEntity.body[0].code == '401_unauthorized'
        responseEntity.body[0].description == '401 Unauthorized. Access Denied'
    }

    @Rollback
    def '/v1/roles/{id} PUT - Standard User with permission "api.roles.update" access granted'() {
        given:
        roleService.addPermission(ROLE_STANDARD_ID, 'api.roles.update')

        Role role = new Role(name:'Role-Name-5', authority:'test.role.authority.5')
        role = roleService.save(role)

        role.name = 'Role-Name-5-Updated'

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<Role> httpEntity = new HttpEntity<Role>(role, headers)

        when:
        ResponseEntity<Role> responseEntity =
                restTemplate
                        .withBasicAuth('standard', 'password')
                        .exchange('/v1/roles/1', HttpMethod.PUT, httpEntity, Role)

        then:
        responseEntity.statusCode.value() == 200
        responseEntity.body.id == role.id
        responseEntity.body.name == 'Role-Name-5-Updated'
        responseEntity.body.authority == 'test.role.authority.5'
    }

    def '/v1/roles/{id} DELETE - Anonymous access denied'() {
        when:
        ResponseEntity<Iterable> responseEntity =
                restTemplate
                        .exchange('/v1/roles/1', HttpMethod.DELETE, null, Iterable, Collections.EMPTY_MAP)

        then:
        responseEntity.statusCode.value() == 401
        responseEntity.body.size() == 1
        responseEntity.body[0].code == '401_unauthorized'
        responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/v1/roles/{id} DELETE - Super User access granted'() {
        given:
        Role role = new Role(name:'Role-Name-5', authority:'test.role.authority.5')
        role = roleService.save(role)

        when:
        ResponseEntity<String> responseEntity =
                restTemplate
                        .withBasicAuth('super', 'password')
                        .exchange("/v1/roles/${role.id}", HttpMethod.DELETE, null, String, Collections.EMPTY_MAP)

        then:
        responseEntity.statusCode.value() == 204
        responseEntity.body == null
    }

    def '/v1/roles/{id} DELETE - Standard User access denied'() {
        when:
        ResponseEntity<Iterable> responseEntity =
                restTemplate
                        .withBasicAuth('standard', 'password')
                        .exchange('/v1/roles/1', HttpMethod.DELETE, null, Iterable, Collections.EMPTY_MAP)

        then:
        responseEntity.statusCode.value() == 401
        responseEntity.body.size() == 1
        responseEntity.body[0].code == '401_unauthorized'
        responseEntity.body[0].description == '401 Unauthorized. Access Denied'
    }

    @Rollback
    def '/v1/roles/{id} DELETE - Standard User with permission "api.roless.delete" access granted'() {
        given:
        roleService.addPermission(ROLE_STANDARD_ID, 'api.roles.delete')

        Role role = new Role(name:'Role-Name-6', authority:'test.role.authority.6')
        role = roleService.save(role)

        when:
        ResponseEntity<String> responseEntity =
                restTemplate
                        .withBasicAuth('standard', 'password')
                        .exchange("/v1/roles/${role.id}", HttpMethod.DELETE, null, String, Collections.EMPTY_MAP)

        then:
        responseEntity.statusCode.value() == 204
        responseEntity.body == null
    }
}
