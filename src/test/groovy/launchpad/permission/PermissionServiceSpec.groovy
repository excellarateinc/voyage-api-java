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
        permission = new Permission(name:'permission.write', description: 'Write permission only')
        modifiedPermission = new Permission(name:'permission.read', description: 'Read permission only')
    }

    def 'Test the list method of PermissionService' () {
        setup:
            permissionRepository.findAll() >> [permission]
        when:
            Iterable<Permission> permissionList = permissionService.listAll()
        then:
            1 == permissionList.size()
    }

    def 'Test save method of PermissionService' () {
        setup:
            permissionRepository.save(_) >> permission
        when:
            Permission savedPermission = permissionService.save(permission)
        then:
            'permission.write' == savedPermission.name
            'Write permission only' == savedPermission.description
    }

    def 'Test update method of PermissionService' () {
        setup:
            permissionRepository.save(_) >> modifiedPermission
        when:
            Permission updatedPermission = permissionService.save(modifiedPermission)
        then:
            'permission.read' == updatedPermission.name
            'Read permission only' == updatedPermission.description
    }

    def 'Test find method of PermissionService' () {
        setup:
            permissionRepository.findOne(_) >> permission
        when:
            Permission fetchedPermission = permissionService.get(1)
        then:
            'permission.write' == fetchedPermission.name
            'Write permission only' == fetchedPermission.description
    }

    def 'Test delete method of PermissionService' () {
        setup:
            permissionRepository.findOne(_) >> permission
            permissionRepository.save(_) >> permission
        when:
            permissionService.delete(1)
        then:
            permission.isDeleted
    }
}
