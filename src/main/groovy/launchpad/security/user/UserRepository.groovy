package launchpad.security.user

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface UserRepository extends CrudRepository<User, Long> {

    @Query('FROM User u WHERE u.username = ?1 AND u.isDeleted = false')
    User findByUsername(String username)

    @Query('FROM User u WHERE u.id = ?1 AND u.isDeleted = false')
    User findOne(Long id)

    @Query('FROM User u WHERE u.verifyCode = ?1 AND u.isDeleted = false')
    User findByVerifyCode(String code)

    @Query('FROM User u WHERE u.isDeleted = false')
    Iterable<User> findAll()
}
