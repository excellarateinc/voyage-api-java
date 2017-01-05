package launchpad.security.token

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface TokenRepository extends CrudRepository<Token, Long> {

    @Query('FROM Token t WHERE t.entityId = ?1 AND t.entityType = ?2 AND t.tokenType = ?3')
    Token find(Long entityId, String entityType, TokenType tokenType)

    @Query('FROM Token t WHERE t.value = ?1')
    Token findByValue(String value)

}
