package launchpad.account

import launchpad.security.user.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface UserSecurityQuestionRepository extends CrudRepository<UserSecurityQuestion, Long> {

    @Query('FROM UserSecurityQuestion u WHERE u.user.id = ?1 AND u.isDeleted = false')
    Iterable<UserSecurityQuestion> findByUserId(Long userId)

    @Query('FROM UserSecurityQuestion u WHERE u.id = ?1 AND u.isDeleted = false')
    UserSecurityQuestion findOne(Long id)

    @Query('FROM UserSecurityQuestion u WHERE u.isDeleted = false')
    Iterable<UserSecurityQuestion> findAll()

/*    @Query("select s FROM SecurityQuestion s JOIN UserSecurityQuestion u WHERE (u.user = ?1) AND u.question.id = s.id")
    Iterable<SecurityQuestion> findSecurityQuestionsForUser(User user)*/
}
