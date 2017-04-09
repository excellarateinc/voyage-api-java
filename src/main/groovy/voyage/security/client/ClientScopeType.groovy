package voyage.security.client

import groovy.transform.EqualsAndHashCode
import org.hibernate.envers.Audited
import voyage.common.AuditableEntity

import javax.persistence.Entity
import javax.validation.constraints.NotNull

@Entity
@Audited
@EqualsAndHashCode(includes=['name', 'description'], callSuper=true)
class ClientScopeType extends AuditableEntity {
    @NotNull
    String name

    @NotNull
    String description
}
