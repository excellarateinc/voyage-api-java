package launchpad.user

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username)

    @Query('FROM User u WHERE u.id = ?1 AND u.isDeleted = false')
    User findOne(Long id)
}
