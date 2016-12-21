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
        when:
            Iterable<Role> roleList = roleService.listAll()
        then:
            1 * roleRepository.findAll() >> [role]
            1 == roleList.size()
    }

    def 'Test save method of RoleService' () {
        when:
            Role savedRole = roleService.save(role)
        then:
            1 * roleRepository.save({ Role role -> role.name == 'Super User' }) >> role
            'Super User' == savedRole.name
            'ROLE_SUPER' == savedRole.authority
    }

    def 'Test update method of RoleService' () {
        when:
            Role updatedRole = roleService.save(modifiedRole)
        then:
            1 * roleRepository.save({ Role role -> role.name == 'Super Admin' }) >> modifiedRole
            'Super Admin' == updatedRole.name
            'ROLE_SUPER' == updatedRole.authority
    }

    def 'Test find method of RoleService' () {
        when:
            Role fetchedRole = roleService.get(1)
        then:
            1 * roleRepository.findOne(_) >> modifiedRole
            'Super Admin' == fetchedRole.name
            'ROLE_SUPER' == fetchedRole.authority
    }

    def 'Test delete method of RoleService' () {
        when:
            roleService.delete(1)
        then:
            1 * roleRepository.delete(_)
    }
}
