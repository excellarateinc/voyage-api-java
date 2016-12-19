package launchpad.role

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class RoleControllerSpec extends Specification {
    Role role
    RoleService roleService = Mock(RoleService)
    RoleController roleController = new RoleController(roleService)

    def setup() {
        role = new Role(id:1, name:'Super User', authority: 'ROLE_SUPER')
    }

    def 'Test to validate LIST method is fetching data from RoleService'() {
        when:
            ResponseEntity<Role> roles = roleController.list()
        then:
            1 * roleService.listAll() >> [role]
            roles != null
            HttpStatus.OK == roles.statusCode

        when:
            roleController.list()
        then:
            1 * roleService.listAll() >> { throw new Exception() }
            thrown(Exception)
    }

    def 'Test to validate FIND method is fetching data from RoleService'() {
        when:
            ResponseEntity<Role> role = roleController.get(1)
        then:
            1 * roleService.get(1) >> role
            role != null
            HttpStatus.OK == role.statusCode
            'Super User' == role.body.name
            'ROLE_SUPER' == role.body.authority

        when:
            roleController.get(1)
        then:
            1 * roleService.get(1) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'Test to validate CREATE method is fetching data from RoleService'() {
        when:
            ResponseEntity<Role> response = roleController.save(role)
        then:
            1 * roleService.save(role) >> role
            response != null
            HttpStatus.CREATED == response.statusCode
            '/v1/roles/1' == response.headers.location[0]
            'Super User' == response.body.name
            'ROLE_SUPER' == response.body.authority

        when:
            roleController.save(role)
        then:
            1 * roleService.save(role) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'Test to validate UPDATE method is fetching data from RoleService'() {
        setup:
            Role modifiedRole = new Role(id:1, name:'Super User', authority: 'ROLE_ADMIN')

        when:
            ResponseEntity<Role> updatedRole = roleController.update(modifiedRole)
        then:
            1 * roleService.update(modifiedRole) >> modifiedRole
            updatedRole != null
            HttpStatus.OK == updatedRole.statusCode

        when:
            roleController.update(modifiedRole)
        then:
            1 * roleService.update(modifiedRole) >> { throw new Exception() }
            thrown(Exception)
    }

    def 'Test to validate DELETE method is fetching data from RoleService'() {
        when:
            ResponseEntity response = roleController.delete(1)
        then:
            1 * roleService.delete(1)
            HttpStatus.OK == response.statusCode

        when:
            roleController.delete(1)
        then:
            1 * roleService.delete(1) >> { throw new Exception() }
            thrown(Exception)
    }

}
