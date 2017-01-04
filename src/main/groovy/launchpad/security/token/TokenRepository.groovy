package launchpad.security.token

import org.springframework.data.repository.CrudRepository

interface TokenRepository extends CrudRepository<Token, Long> {

}
