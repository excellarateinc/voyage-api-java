package launchpad.permission

import launchpad.permission.Permission
import launchpad.permission.PermissionController
import launchpad.permission.PermissionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class PermissionControllerSpec extends Specification {
    Permission permission
    PermissionService permissionService = Mock(PermissionService)
    PermissionController permissionController = new PermissionController(permissionService)

    def setup() {
        permission = new Permission(id:1, name:'permission.write', description: 'Write permission only')
    }

    def 'Test to validate LIST method is fetching data from PermissionService'() {
        when:
            ResponseEntity<Permission> permissions = permissionController.list()
        then:
            1 * permissionService.listAll() >> [permission]
        permissions != null
            HttpStatus.OK == permissions.statusCode

        when:
            permissionController.list()
        then:
            1 * permissionService.listAll() >> { throw new Exception() }
            thrown(Exception)
    }

    def 'Test to validate FIND method is fetching data from PermissionService'() {
        when:
            ResponseEntity<Permission> permission = permissionController.get(1)
        then:
            1 * permissionService.get(1) >> permission
        permission != null
            HttpStatus.OK == permission.statusCode
            'permission.write' == permission.body.name
            'Write permission only' == permission.body.description

        when:
            permissionController.get(1)
        then:
            1 * permissionService.get(1) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'Test to validate CREATE method is fetching data from PermissionService'() {
        when:
            ResponseEntity<Permission> response = permissionController.save(permission)
        then:
            1 * permissionService.save(permission) >> permission
            response != null
            HttpStatus.CREATED == response.statusCode
            '/v1/permissions/1' == response.headers.location[0]
            'permission.write' == response.body.name
            'Write permission only' == response.body.description

        when:
            permissionController.save(permission)
        then:
            1 * permissionService.save(permission) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'Test to validate UPDATE method is fetching data from PermissionService'() {
        setup:
            Permission modifiedPermission = new Permission(id:1, name:'permission.write', description: 'Write permission only')

        when:
            ResponseEntity<Permission> updatedPermission = permissionController.update(modifiedPermission)
        then:
            1 * permissionService.update(modifiedPermission) >> modifiedPermission
            updatedPermission != null
            HttpStatus.OK == updatedPermission.statusCode

        when:
            permissionController.update(modifiedPermission)
        then:
            1 * permissionService.update(modifiedPermission) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'Test to validate DELETE method is fetching data from PermissionService'() {
        when:
            ResponseEntity response = permissionController.delete(1)
        then:
            1 * permissionService.delete(1)
            HttpStatus.OK == response.statusCode

        when:
            permissionController.delete(1)
        then:
            1 * permissionService.delete(1) >> { throw new Exception() }
            thrown(Exception)
    }

}
