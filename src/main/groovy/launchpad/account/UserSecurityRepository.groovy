package launchpad.account

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface UserSecurityRepository extends CrudRepository<UserSecurityAnswer, Long> {

    @Query('FROM UserSecurityAnswer u WHERE u.user_id = ?1')
    Iterable <UserSecurityAnswer> findByUserId(Long userid)
}