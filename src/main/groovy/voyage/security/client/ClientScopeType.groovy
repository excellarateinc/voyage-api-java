package voyage.security.client

import org.hibernate.envers.Audited
import voyage.common.AuditableEntity

import javax.persistence.Entity
import javax.validation.constraints.NotNull

@Entity
@Audited
class ClientScopeType extends AuditableEntity {
    @NotNull
    String name

    @NotNull
    String description
}
