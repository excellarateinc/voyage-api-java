package launchpad.role

import spock.lang.Specification

class RoleServiceSpec extends Specification {
    Role role
    Role modifiedRole
    RoleRepository roleRepository = Mock()
    RoleService roleService = new RoleService(roleRepository)

    def setup() {
        role = new Role(name:'Super User', authority: 'ROLE_SUPER')
        modifiedRole = new Role(name:'Super Admin', authority: 'ROLE_SUPER')
    }

    def 'Test the list method of RoleService' () {
        setup:
            roleRepository.findAll() >> [role]
        when:
            Iterable<Role> roleList = roleService.listAll()
        then:
            1 == roleList.size()
    }

    def 'Test save method of RoleService' () {
        setup:
            roleRepository.save(_) >> role
        when:
            Role savedRole = roleService.save(role)
        then:
            'Super User' == savedRole.name
            'ROLE_SUPER' == savedRole.authority
    }

    def 'Test update method of RoleService' () {
        setup:
            roleRepository.save(_) >> modifiedRole
        when:
            Role updatedRole = roleService.save(modifiedRole)
        then:
            'Super Admin' == updatedRole.name
            'ROLE_SUPER' == updatedRole.authority
    }

    def 'Test find method of RoleService' () {
        setup:
            roleRepository.findOne(_) >> role
        when:
            Role fetchedRole = roleService.get(1)
        then:
            'Super User' == fetchedRole.name
            'ROLE_SUPER' == fetchedRole.authority
    }

    def 'Test delete method of RoleService' () {
        setup:
            roleRepository.findOne(_) >> role
        when:
            roleService.delete(1)
        then:
            role.isDeleted
    }
}
