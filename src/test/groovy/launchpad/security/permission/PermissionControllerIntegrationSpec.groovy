package launchpad.security.permission

import launchpad.error.UnknownIdentifierException
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
class PermissionControllerIntegrationSpec extends Specification {
    private static final Long ROLE_STANDARD_ID = 3

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private PermissionService permissionService

    @Autowired
    private RoleService roleService

    def '/v1/permissions GET - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .getForEntity('/v1/permissions', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/v1/permissions GET - Super User access granted'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .withBasicAuth('super', 'password')
                            .getForEntity('/v1/permissions', Iterable)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.size() == 15
            responseEntity.body[0].id == 1L
            responseEntity.body[0].name == 'api.users.list'
            responseEntity.body[0].description == '/users GET web service endpoint to return a full list of users'
    }

    def '/v1/permissions GET - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .withBasicAuth('standard', 'password')
                            .getForEntity('/v1/permissions', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Access Denied'
    }

    def '/v1/permissions GET - Standard User with permission "api.permissions.list" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.permissions.list')

        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .withBasicAuth('standard', 'password')
                            .getForEntity('/v1/permissions', Iterable)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.size() == 15
            responseEntity.body[0].id == 1L
            responseEntity.body[0].name == 'api.users.list'
            responseEntity.body[0].description == '/users GET web service endpoint to return a full list of users'
    }

    def '/v1/permissions POST - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .postForEntity('/v1/permissions', null, Iterable, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/v1/permissions POST - Super User access granted'() {
        given:
            Permission permission = new Permission(name:'Permission-Name-1', description:'test permission 1')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Permission> responseEntity =
                    restTemplate
                            .withBasicAuth('super', 'password')
                            .postForEntity('/v1/permissions', httpEntity, Permission)

        then:
            responseEntity.statusCode.value() == 201
            responseEntity.headers.getFirst('location') == '/v1/permissions/16'
            responseEntity.body.id
            responseEntity.body.name == 'Permission-Name-1'
            responseEntity.body.description == 'test permission 1'

            // Delete the permission created by this test
            deletePermission('Permission-Name-1')
    }

    def '/v1/permissions POST - Standard User access denied'() {
        given:
            Permission permission = new Permission(name:'Permission-Name-2', description:'test permission 2')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Iterable> responseEntity =
                    restTemplate
                            .withBasicAuth('standard', 'password')
                            .postForEntity('/v1/permissions', httpEntity, Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Access Denied'
    }

    def '/v1/permissions POST - Standard User with permission "api.permissions.create" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.permissions.create')

            Permission permission = new Permission(name:'Permission-Name-3', description:'test permission 3')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Permission> responseEntity =
                restTemplate
                    .withBasicAuth('standard', 'password')
                    .postForEntity('/v1/permissions', httpEntity, Permission)

        then:
            responseEntity.statusCode.value() == 201
            responseEntity.headers.getFirst('location') == '/v1/permissions/17'
            responseEntity.body.id
            responseEntity.body.name == 'Permission-Name-3'
            responseEntity.body.description == 'test permission 3'

            // Delete the permission created by this test
            deletePermission('Permission-Name-3')
    }

    def '/v1/permissions/{id} GET - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                restTemplate
                    .getForEntity('/v1/permissions/1', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/v1/permissions/{id} GET - Super User access granted'() {
        when:
            ResponseEntity<Permission> responseEntity =
                restTemplate
                    .withBasicAuth('super', 'password')
                    .getForEntity('/v1/permissions/1', Permission)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == 1L
            responseEntity.body.name == 'api.users.list'
            responseEntity.body.description == '/users GET web service endpoint to return a full list of users'
    }

    def '/v1/permissions/{id} GET - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                restTemplate
                    .withBasicAuth('standard', 'password')
                    .getForEntity('/v1/permissions/1', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Access Denied'
    }

    def '/v1/permissions/{id} GET - Standard User with permission "api.permissions.get" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.permissions.get')

        when:
            ResponseEntity<Permission> responseEntity =
                restTemplate
                    .withBasicAuth('standard', 'password')
                    .getForEntity('/v1/permissions/1', Permission)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == 1L
            responseEntity.body.name == 'api.users.list'
            responseEntity.body.description == '/users GET web service endpoint to return a full list of users'
    }

    def '/v1/permissions/{id} GET - Invalid ID returns a 400 Bad Request response'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                restTemplate
                    .withBasicAuth('super', 'password')
                    .getForEntity('/v1/permissions/999999', Iterable)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '400_bad_request'
            responseEntity.body[0].description == 'Unknown record identifier provided'
    }

    def '/v1/permissions/{id} PUT - Anonymous access denied'() {
        given:
            Permission permission = new Permission(name:'Permission-Name-4', description:'test permission 4')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Iterable> responseEntity =
                restTemplate
                    .exchange('/v1/permissions/1', HttpMethod.PUT, httpEntity, Iterable, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'

            // Delete the permission created by this test
            deletePermission('Permission-Name-1')
    }

    def '/v1/permissions/{id} PUT - Super User access granted'() {
        given:
            Permission permission = new Permission(name:'Permission-Name-4', description:'test permission 4')
            permission = permissionService.save(permission)

            permission.name = 'Permission-Name-4-Updated'

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Permission> responseEntity =
                restTemplate
                    .withBasicAuth('super', 'password')
                    .exchange('/v1/permissions/1', HttpMethod.PUT, httpEntity, Permission)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == permission.id
            responseEntity.body.name == 'Permission-Name-4-Updated'
            responseEntity.body.description == 'test permission 4'

            // Delete the permission created by this test
            deletePermission('Permission-Name-4-Updated')
    }

    def '/v1/permissions/{id} PUT - Standard User access denied'() {
        given:
            Permission permission = new Permission(name:'Permission-Name-4', description:'test permission 4')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Iterable> responseEntity =
                restTemplate
                    .withBasicAuth('standard', 'password')
                    .exchange('/v1/permissions/1', HttpMethod.PUT, httpEntity, Iterable, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Access Denied'

            // Delete the permission created by this test
            deletePermission('Permission-Name-4')
    }

    def '/v1/permissions/{id} PUT - Standard User with permission "api.permissions.update" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.permissions.update')

            Permission permission = new Permission(name:'Permission-Name-5', description:'test permission 5')
            permission = permissionService.save(permission)

            permission.name = 'Permission-Name-5-Updated'

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Permission> responseEntity =
                restTemplate
                    .withBasicAuth('standard', 'password')
                    .exchange('/v1/permissions/1', HttpMethod.PUT, httpEntity, Permission)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == permission.id
            responseEntity.body.name == 'Permission-Name-5-Updated'
            responseEntity.body.description == 'test permission 5'

            // Delete the permission created by this method
            deletePermission('Permission-Name-5-Updated')
    }

    def '/v1/permissions/{id} PUT - Invalid ID returns a 400 Bad Request response'() {
        given:
            Permission permission = new Permission(id:9999, name:'Permission-Name-4', description:'test permission 4')

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Iterable> responseEntity =
                restTemplate
                    .withBasicAuth('super', 'password')
                    .exchange('/v1/permissions/9999', HttpMethod.PUT, httpEntity, Iterable)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body[0].code == "400_bad_request"
            responseEntity.body[0].description == 'Unknown record identifier provided'
    }

    def '/v1/permissions/{id} PUT - Updating an immutable permission returns a 400 Bad Request response'() {
        given:
            Permission permission = new Permission(id: 1, name:'Permission-Name-4', description:'test permission 4')

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Iterable> responseEntity =
                restTemplate
                    .withBasicAuth('super', 'password')
                    .exchange('/v1/permissions/1', HttpMethod.PUT, httpEntity, Iterable)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '400_bad_request'
            responseEntity.body[0].description == 'The requested record is immutable. No changes to this record are allowed.'
    }

    def '/v1/permissions/{id} DELETE - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                restTemplate
                    .exchange('/v1/permissions/1', HttpMethod.DELETE, null, Iterable, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/v1/permissions/{id} DELETE - Super User access granted'() {
        given:
            Permission permission = new Permission(name:'Permission-Name-6', description:'test permission 6')
            permission = permissionService.save(permission)

        when:
            ResponseEntity<String> responseEntity =
                restTemplate
                    .withBasicAuth('super', 'password')
                    .exchange("/v1/permissions/${permission.id}", HttpMethod.DELETE, null, String, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null

            // Delete the permission created by this method
            deletePermission('Permission-Name-6')
    }

    def '/v1/permissions/{id} DELETE - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                restTemplate
                    .withBasicAuth('standard', 'password')
                    .exchange('/v1/permissions/1', HttpMethod.DELETE, null, Iterable, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].code == '401_unauthorized'
            responseEntity.body[0].description == '401 Unauthorized. Access Denied'
    }

    def '/v1/permissions/{id} DELETE - Standard User with permission "api.permissions.delete" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.permissions.delete')

            Permission permission = new Permission(name:'Permission-Name-7', description:'test permission 7')
            permission = permissionService.save(permission)

        when:
            ResponseEntity<String> responseEntity =
                restTemplate
                    .withBasicAuth('standard', 'password')
                    .exchange("/v1/permissions/${permission.id}", HttpMethod.DELETE, null, String, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null

            // Delete the permission created by this method
            deletePermission('Permission-Name-7')
    }

    def '/v1/permissions/{id} DELETE - Invalid ID returns a 400 Bad Request response'() {
        when:
           ResponseEntity<Iterable> responseEntity =
                restTemplate
                        .withBasicAuth('super', 'password')
                        .exchange("/v1/permissions/9999", HttpMethod.DELETE, null, Iterable, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].code == "400_bad_request"
            responseEntity.body[0].description == "Unknown record identifier provided"
    }

    def '/v1/permissions/{id} DELETE - Deleting an immutable permission returns a 400 Bad Request response'() {
        when:
            ResponseEntity<Iterable> responseEntity =
                restTemplate
                    .withBasicAuth('super', 'password')
                    .exchange("/v1/permissions/1", HttpMethod.DELETE, null, Iterable, Collections.EMPTY_MAP)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].code == "400_bad_request"
            responseEntity.body[0].description == "The requested record is immutable. No changes to this record are allowed."
    }

    private void deletePermission(String permissionName) {
        try {
            Permission permissionToDelete = permissionService.findByName(permissionName)
            permissionService.delete(permissionToDelete.id)
        } catch (UnknownIdentifierException ignore) {
        }
    }
}
