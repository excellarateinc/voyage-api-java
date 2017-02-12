package voyage.security.client

import voyage.common.AuditableEntity

import javax.persistence.Entity
import javax.validation.constraints.NotNull

@Entity
class ClientScopeType extends AuditableEntity {
    @NotNull
    String name

    @NotNull
    String description
}
