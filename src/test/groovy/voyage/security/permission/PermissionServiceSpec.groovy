package voyage.security.permission

import spock.lang.Specification

class PermissionServiceSpec extends Specification {
    Permission permission
    Permission modifiedPermission
    PermissionRepository permissionRepository = Mock()
    PermissionService permissionService = new PermissionService(permissionRepository)

    def setup() {
        permission = new Permission(name:'permission.write', description:'Write permission only')
        modifiedPermission = new Permission(name:'permission.read', description:'Read permission only')
    }

    def 'listAll - returns a single result' () {
        setup:
            permissionRepository.findAll() >> [permission]
        when:
            Iterable<Permission> permissionList = permissionService.listAll()
        then:
            1 == permissionList.size()
    }

    def 'save - applies the values and calls the permissionRepository' () {
        setup:
            permissionRepository.save(_) >> permission
        when:
            Permission savedPermission = permissionService.saveDetached(permission)
        then:
            'permission.write' == savedPermission.name
            'Write permission only' == savedPermission.description
            !savedPermission.isDeleted
    }

    def 'get - calls the permissionRepository.findOne' () {
        setup:
            permissionRepository.findOne(_) >> permission
        when:
            Permission fetchedPermission = permissionService.get(1)
        then:
            'permission.write' == fetchedPermission.name
            'Write permission only' == fetchedPermission.description
            !fetchedPermission.isDeleted
    }

    def 'delete - verifies the object and calls permissionRepository.delete' () {
        setup:
            permissionRepository.findOne(_) >> permission
            permissionRepository.save(_) >> permission
        when:
            permissionService.delete(1)
        then:
            permission.isDeleted
    }
}
