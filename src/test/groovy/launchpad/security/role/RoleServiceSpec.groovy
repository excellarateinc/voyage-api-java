package launchpad.security.role

import launchpad.security.permission.PermissionService
import spock.lang.Specification

class RoleServiceSpec extends Specification {
    Role role
    Role modifiedRole
    RoleRepository roleRepository = Mock()
    PermissionService permissionService = Mock()
    RoleService roleService = new RoleService(roleRepository, permissionService)

    def setup() {
        role = new Role(name:'Super User', authority:'ROLE_SUPER')
        modifiedRole = new Role(name:'Super Admin', authority:'ROLE_SUPER')
    }

    def 'listAll - returns a single result' () {
        setup:
            roleRepository.findAll() >> [role]
        when:
            Iterable<Role> roleList = roleService.listAll()
        then:
            1 == roleList.size()
    }

    def 'save - applies the values and calls the roleRepository' () {
        setup:
            roleRepository.save(_) >> role
        when:
            Role savedRole = roleService.save(role)
        then:
            'Super User' == savedRole.name
            'ROLE_SUPER' == savedRole.authority
            !savedRole.isDeleted
    }

    def 'get - calls the roleRepository.findOne' () {
        setup:
            roleRepository.findOne(_) >> role
        when:
            Role fetchedRole = roleService.get(1)
        then:
            'Super User' == fetchedRole.name
            'ROLE_SUPER' == fetchedRole.authority
            !fetchedRole.isDeleted
    }

    def 'delete - verifies the object and calls roleRepository.delete' () {
        setup:
            roleRepository.findOne(_) >> role
        when:
            roleService.delete(1)
        then:
            role.isDeleted
    }

    def 'addPermission - inserts the permission if it does not already exist'() {
        setup:
            roleRepository.save(_) >> role
        when:
            Role savedRole = roleService.save(role)
        then:
            'Super User' == savedRole.name
            'ROLE_SUPER' == savedRole.authority
            !savedRole.isDeleted
    }
}
