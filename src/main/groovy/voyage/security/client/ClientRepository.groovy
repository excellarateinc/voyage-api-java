package voyage.security.client

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface ClientRepository  extends CrudRepository<Client, Long> {

    @Query('FROM Client c WHERE c.clientIdentifier = ?1 AND c.isDeleted = false')
    Client findByClientIdentifier(String clientIdentifier)

}
