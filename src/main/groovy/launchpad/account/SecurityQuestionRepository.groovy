package launchpad.account

import org.springframework.data.repository.CrudRepository

interface SecurityQuestionRepository extends CrudRepository<SecurityQuestion, Long> {

}