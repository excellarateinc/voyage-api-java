package launchpad.permission

import launchpad.permission.Permission
import launchpad.permission.PermissionRepository
import launchpad.permission.PermissionService
import spock.lang.Specification

class PermissionServiceSpec extends Specification {
    Permission permission
    Permission modifiedPermission
    PermissionRepository permissionRepository = Mock()
    PermissionService permissionService = new PermissionService(permissionRepository)

    def setup() {
        permission = new Permission(id:1, name:'permission.write', description: 'Write permission only')
        modifiedPermission = new Permission(id:1, name:'permission.read', description: 'Read permission only')
    }

    def 'Test the list method of PermissionService' () {
        when:
            Iterable<Permission> permissionList = permissionService.listAll()
        then:
            1 * permissionRepository.findAll() >> [permission]
            1 == permissionList.size()
    }

    def 'Test save method of PermissionService' () {
        when:
        Permission savedPermission = permissionService.save(permission)
        then:
            1 * permissionRepository.save({ Permission permission -> permission.name == 'permission.write' }) >> permission
            'permission.write' == savedPermission.name
            'Write permission only' == savedPermission.description
    }

    def 'Test update method of PermissionService' () {
        when:
        Permission updatedPermission = permissionService.update(modifiedPermission)
        then:
            1 * permissionRepository.save({ Permission permission -> permission.name == 'permission.read' }) >> modifiedPermission
            'permission.read' == updatedPermission.name
            'Read permission only' == updatedPermission.description
    }

    def 'Test find method of PermissionService' () {
        when:
        Permission fetchedPermission = permissionService.get(1)
        then:
            1 * permissionRepository.findOne(_) >> modifiedPermission
            'permission.write' == fetchedPermission.name
            'Write permission only' == fetchedPermission.description
    }

    def 'Test delete method of PermissionService' () {
        when:
        permissionService.delete(1)
        then:
            1 * permissionRepository.delete(_)
    }
}
