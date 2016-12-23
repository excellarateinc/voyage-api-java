package launchpad.permission

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PermissionRepository extends CrudRepository<Permission, Long> {

    @Query('FROM Permission p WHERE p.id = ?1 AND p.isDeleted = false')
    Permission findOne(Long id)

    @Query('FROM Permission p WHERE p.isDeleted = false')
    Iterable<Permission> findAll()
}
