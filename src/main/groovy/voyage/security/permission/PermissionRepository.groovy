package voyage.security.permission

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PermissionRepository extends CrudRepository<Permission, Long> {

    @Query('FROM Permission p WHERE p.id = ?1 AND p.isDeleted = false')
    Permission findOne(Long id)

    @Query('FROM Permission p WHERE p.isDeleted = false')
    Iterable<Permission> findAll()

    @Query('''SELECT permission
                FROM User as user
                JOIN user.roles as role
                JOIN role.permissions as permission
                WHERE user.id = ?1
                AND user.isDeleted = false
                AND role.isDeleted = false
                AND permission.isDeleted = false
                ORDER BY permission.name ASC''')
    Iterable<Permission> findAllByUserId(Long id)

    @Query('''SELECT permission
                FROM Client as client
                JOIN client.roles as role
                JOIN role.permissions as permission
                WHERE client.id = ?1
                AND client.isDeleted = false
                AND role.isDeleted = false
                AND permission.isDeleted = false
                ORDER BY permission.name ASC''')
    Iterable<Permission> findAllByClientId(Long id)

    @Query('FROM Permission p WHERE p.name =?1 AND p.isDeleted = false')
    Permission findByName(String name)
}
