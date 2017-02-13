package launchpad.account

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface UserSecurityQuestionRepository extends CrudRepository<UserSecurityQuestion, Long> {

    @Query('FROM UserSecurityQuestion u WHERE u.user.id = ?1 AND u.isDeleted = false')
    Iterable<UserSecurityQuestion> findByUserId(Long userId)

    @Query('FROM UserSecurityQuestion u WHERE u.id = ?1 AND u.isDeleted = false')
    UserSecurityQuestion findOne(Long id)

    @Query('FROM UserSecurityQuestion u WHERE u.isDeleted = false')
    Iterable<UserSecurityQuestion> findAll()
}
