package launchpad.account

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface SecurityQuestionRepository extends CrudRepository<SecurityQuestion, Long> {

    @Query('FROM SecurityQuestion s WHERE s.id = ?1 AND s.isDeleted = false')
    SecurityQuestion findOne(Long id)

    @Query('FROM SecurityQuestion s WHERE s.isDeleted = false')
    Iterable<SecurityQuestion> findAll()
}
