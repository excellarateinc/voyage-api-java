package voyage.security.user

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface UserRepository extends CrudRepository<User, Long> {

    @Query('FROM User u WHERE u.username = ?1 AND u.isDeleted = false')
    User findByUsername(String username)

    @Query('FROM User u WHERE u.id = ?1 AND u.isDeleted = false')
    User findOne(Long id)

    @Query('FROM User u WHERE u.isDeleted = false')
    Iterable<User> findAll()

    @Query( "SELECT u FROM User u INNER JOIN u.roles r where r.authority in ?1" )
    Iterable<User> findAllByRolesInList(List<String> roles)
}
