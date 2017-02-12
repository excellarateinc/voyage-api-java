package voyage.security.permission

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import voyage.error.UnknownIdentifierException
import voyage.security.role.RoleService
import voyage.test.AbstractIntegrationTest

class PermissionControllerIntegrationSpec extends AbstractIntegrationTest {
    private static final Long ROLE_STANDARD_ID = 2

    @Autowired
    private PermissionService permissionService

    @Autowired
    private RoleService roleService

    def '/api/v1/permissions GET - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/permissions', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/permissions GET - Super User access granted'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/permissions', Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.size() == 15
            responseEntity.body[0].id == 1L
            responseEntity.body[0].name == 'api.users.list'
            responseEntity.body[0].description == '/users GET web service endpoint to return a full list of users'
    }

    def '/api/v1/permissions GET - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/permissions', Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def '/api/v1/permissions GET - Standard User with permission "api.permissions.list" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.permissions.list')

        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/permissions', Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.size() == 15
            responseEntity.body[0].id == 1L
            responseEntity.body[0].name == 'api.users.list'
            responseEntity.body[0].description == '/users GET web service endpoint to return a full list of users'
    }

    def '/api/v1/permissions POST - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = POST('/api/v1/permissions', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/permissions POST - Super User access granted'() {
        given:
            Permission permission = new Permission(name:'Permission-Name-1', description:'test permission 1')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Permission> responseEntity = POST('/api/v1/permissions', httpEntity, Permission, superClient)

        then:
            responseEntity.statusCode.value() == 201
            responseEntity.headers.getFirst('location') == '/api/v1/permissions/16'
            responseEntity.body.id
            responseEntity.body.name == 'Permission-Name-1'
            responseEntity.body.description == 'test permission 1'

            // Delete the permission created by this test
            deletePermission('Permission-Name-1')
    }

    def '/api/v1/permissions POST - Standard User access denied'() {
        given:
            Permission permission = new Permission(name:'Permission-Name-2', description:'test permission 2')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Iterable> responseEntity = POST('/api/v1/permissions', httpEntity, Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def '/api/v1/permissions POST - Standard User with permission "api.permissions.create" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.permissions.create')

            Permission permission = new Permission(name:'Permission-Name-3', description:'test permission 3')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Permission> responseEntity = POST('/api/v1/permissions', httpEntity, Permission, standardClient)

        then:
            responseEntity.statusCode.value() == 201
            responseEntity.headers.getFirst('location') == '/api/v1/permissions/17'
            responseEntity.body.id
            responseEntity.body.name == 'Permission-Name-3'
            responseEntity.body.description == 'test permission 3'

            // Delete the permission created by this test
            deletePermission('Permission-Name-3')
    }

    def '/api/v1/permissions/{id} GET - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/permissions/1', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/permissions/{id} GET - Super User access granted'() {
        when:
            ResponseEntity<Permission> responseEntity = GET('/api/v1/permissions/1', Permission, superClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == 1L
            responseEntity.body.name == 'api.users.list'
            responseEntity.body.description == '/users GET web service endpoint to return a full list of users'
    }

    def '/api/v1/permissions/{id} GET - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/permissions/1', Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def '/api/v1/permissions/{id} GET - Standard User with permission "api.permissions.get" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.permissions.get')

        when:
            ResponseEntity<Permission> responseEntity = GET('/api/v1/permissions/1', Permission, standardClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == 1L
            responseEntity.body.name == 'api.users.list'
            responseEntity.body.description == '/users GET web service endpoint to return a full list of users'
    }

    def '/api/v1/permissions/{id} GET - Invalid ID returns a 400 Bad Request response'() {
        when:
            ResponseEntity<Iterable> responseEntity = GET('/api/v1/permissions/999999', Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '400_bad_request'
            responseEntity.body[0].errorDescription == 'Unknown record identifier provided'
    }

    def '/api/v1/permissions/{id} PUT - Anonymous access denied'() {
        given:
            Permission permission = new Permission(name:'Permission-Name-4', description:'test permission 4')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Iterable> responseEntity = PUT('/api/v1/permissions/1', httpEntity, Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'

            // Delete the permission created by this test
            deletePermission('Permission-Name-1')
    }

    def '/api/v1/permissions/{id} PUT - Super User access granted'() {
        given:
            Permission permission = new Permission(name:'Permission-Name-4', description:'test permission 4')
            permission = permissionService.saveDetached(permission)

            Permission permissionUpdate = new Permission()
            permissionUpdate.id = permission.id
            permissionUpdate.name = 'Permission-Name-4-Updated'
            permissionUpdate.description = permission.description
            
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permissionUpdate, headers)

        when:
            ResponseEntity<Permission> responseEntity = PUT('/api/v1/permissions/1', httpEntity, Permission, superClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == permission.id
            responseEntity.body.name == 'Permission-Name-4-Updated'
            responseEntity.body.description == 'test permission 4'

            // Delete the permission created by this test
            deletePermission('Permission-Name-4-Updated')
    }

    def '/api/v1/permissions/{id} PUT - Standard User access denied'() {
        given:
            Permission permission = new Permission(name:'Permission-Name-4', description:'test permission 4')
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Iterable> responseEntity = PUT('/api/v1/permissions/1', httpEntity, Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'

            // Delete the permission created by this test
            deletePermission('Permission-Name-4')
    }

    def '/api/v1/permissions/{id} PUT - Standard User with permission "api.permissions.update" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.permissions.update')

            Permission permission = new Permission(name:'Permission-Name-5', description:'test permission 5', createdBy:'test', lastModifiedBy:'test')
            permission = permissionService.saveDetached(permission)

            permission.name = 'Permission-Name-5-Updated'

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Permission> responseEntity = PUT('/api/v1/permissions/1', httpEntity, Permission, standardClient)

        then:
            responseEntity.statusCode.value() == 200
            responseEntity.body.id == permission.id
            responseEntity.body.name == 'Permission-Name-5-Updated'
            responseEntity.body.description == 'test permission 5'

            // Delete the permission created by this method
            deletePermission('Permission-Name-5-Updated')
    }

    def '/api/v1/permissions/{id} PUT - Invalid ID returns a 400 Bad Request response'() {
        given:
            Permission permission = new Permission(id:9999, name:'Permission-Name-4', description:'test permission 4')

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Iterable> responseEntity = PUT('/api/v1/permissions/9999', httpEntity, Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body[0].error == '400_bad_request'
            responseEntity.body[0].errorDescription == 'Unknown record identifier provided'
    }

    def '/api/v1/permissions/{id} PUT - Updating an immutable permission returns a 400 Bad Request response'() {
        given:
            Permission permission = new Permission(id:1, name:'Permission-Name-4', description:'test permission 4')

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            HttpEntity<Permission> httpEntity = new HttpEntity<Permission>(permission, headers)

        when:
            ResponseEntity<Iterable> responseEntity = PUT('/api/v1/permissions/1', httpEntity, Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '400_bad_request'
            responseEntity.body[0].errorDescription == 'The requested record is immutable. No changes to this record are allowed.'
    }

    def '/api/v1/permissions/{id} DELETE - Anonymous access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = DELETE('/api/v1/permissions/1', Iterable)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Full authentication is required to access this resource'
    }

    def '/api/v1/permissions/{id} DELETE - Super User access granted'() {
        given:
            Permission permission = new Permission(name:'Permission-Name-6', description:'test permission 6')
            permission = permissionService.saveDetached(permission)

        when:
            ResponseEntity<String> responseEntity = DELETE("/api/v1/permissions/${permission.id}", String, superClient)

        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null

            // Delete the permission created by this method
            deletePermission('Permission-Name-6')
    }

    def '/api/v1/permissions/{id} DELETE - Standard User access denied'() {
        when:
            ResponseEntity<Iterable> responseEntity = DELETE('/api/v1/permissions/1', Iterable, standardClient)

        then:
            responseEntity.statusCode.value() == 401
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '401_unauthorized'
            responseEntity.body[0].errorDescription == '401 Unauthorized. Access Denied'
    }

    def '/api/v1/permissions/{id} DELETE - Standard User with permission "api.permissions.delete" access granted'() {
        given:
            roleService.addPermission(ROLE_STANDARD_ID, 'api.permissions.delete')

            Permission permission = new Permission(name:'Permission-Name-7', description:'test permission 7')
            permission = permissionService.saveDetached(permission)

        when:
            ResponseEntity<String> responseEntity = DELETE("/api/v1/permissions/${permission.id}", String, standardClient)

        then:
            responseEntity.statusCode.value() == 204
            responseEntity.body == null

            // Delete the permission created by this method
            deletePermission('Permission-Name-7')
    }

    def '/api/v1/permissions/{id} DELETE - Invalid ID returns a 400 Bad Request response'() {
        when:
           ResponseEntity<Iterable> responseEntity = DELETE('/api/v1/permissions/9999', Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '400_bad_request'
            responseEntity.body[0].errorDescription == 'Unknown record identifier provided'
    }

    def '/api/v1/permissions/{id} DELETE - Deleting an immutable permission returns a 400 Bad Request response'() {
        when:
            ResponseEntity<Iterable> responseEntity = DELETE('/api/v1/permissions/1', Iterable, superClient)

        then:
            responseEntity.statusCode.value() == 400
            responseEntity.body.size() == 1
            responseEntity.body[0].error == '400_bad_request'
            responseEntity.body[0].errorDescription == 'The requested record is immutable. No changes to this record are allowed.'
    }

    private void deletePermission(String permissionName) {
        try {
            Permission permissionToDelete = permissionService.findByName(permissionName)
            permissionService.delete(permissionToDelete.id)
        } catch (UnknownIdentifierException ignore) {
        }
    }
}
