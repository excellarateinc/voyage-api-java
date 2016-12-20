package launchpad.role

import org.springframework.data.repository.CrudRepository

interface PermissionRepository extends CrudRepository<Permission, Long> {

}
