package voyage.security.role

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface RoleRepository extends CrudRepository<Role, Long> {

    @Query('FROM Role r WHERE r.id = ?1 AND r.isDeleted = false')
    Role findOne(Long id)

    @Query('FROM Role r WHERE r.isDeleted = false')
    Iterable<Role> findAll()
}
