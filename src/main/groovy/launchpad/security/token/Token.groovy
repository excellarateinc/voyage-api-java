package launchpad.security.token

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity
class Token {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id

    @NotNull
    String value

    @NotNull
    TokenType tokenType

    @NotNull
    String entityType

    @NotNull
    Long entityId

    Date expiresOn

    boolean isExpired() {
        return expiresOn != null && expiresOn < new Date()
    }
}
