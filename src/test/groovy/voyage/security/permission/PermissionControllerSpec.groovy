package voyage.security.permission

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

// TODO Needs to be rewritten to focus on inputs and outputs. Right now these tests are not validating the entire JSON response or JSON request
// TODO Remove the Exception tests because they do NOTHING! What's the point of these at all?
// TODO Add NEW exception tests for exceptions that are actually thrown by the Service classes (ImmutableRecordException,
//      UnknownIdentifierException, ValidationException...)
class PermissionControllerSpec extends Specification {
    Permission permission
    PermissionService permissionService = Mock(PermissionService)
    PermissionController permissionController = new PermissionController(permissionService)

    def setup() {
        permission = new Permission(id:1, name:'permission.write', description:'Write permission only')
    }

    def 'list - fetch data from PermissionService'() {
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

    def 'get - fetch data from PermissionService'() {
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

    def 'save - calling permissionService to save object'() {
        when:
            ResponseEntity<Permission> response = permissionController.save(permission)
        then:
            1 * permissionService.saveDetached(permission) >> permission
            response != null
            HttpStatus.CREATED == response.statusCode
            '/api/v1/permissions/1' == response.headers.location[0]
            'permission.write' == response.body.name
            'Write permission only' == response.body.description

        when:
            permissionController.save(permission)
        then:
            1 * permissionService.saveDetached(permission) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'update - calling permissionService to save incoming object'() {
        setup:
            Permission modifiedPermission = new Permission(id:1, name:'permission.write', description:'Write permission only')

        when:
            ResponseEntity<Permission> updatedPermission = permissionController.update(modifiedPermission)
        then:
            1 * permissionService.saveDetached(modifiedPermission) >> modifiedPermission
            updatedPermission != null
            HttpStatus.OK == updatedPermission.statusCode

        when:
            permissionController.update(modifiedPermission)
        then:
            1 * permissionService.saveDetached(modifiedPermission) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'delete - calling permissionService with the permission id'() {
        when:
            ResponseEntity response = permissionController.delete(1)
        then:
            1 * permissionService.delete(1)
            HttpStatus.NO_CONTENT == response.statusCode

        when:
            permissionController.delete(1)
        then:
            1 * permissionService.delete(1) >> { throw new Exception() }
            thrown(Exception)
    }

}
