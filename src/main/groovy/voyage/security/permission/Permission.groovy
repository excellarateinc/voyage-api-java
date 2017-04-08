package voyage.security.permission

import groovy.transform.EqualsAndHashCode
import org.hibernate.envers.Audited
import voyage.common.AuditableEntity

import javax.persistence.Entity
import javax.validation.constraints.NotNull

@Entity
@Audited
@EqualsAndHashCode(includes=['name'], callSuper=true)
class Permission extends AuditableEntity {
    @NotNull
    String name

    String description

    @NotNull
    Boolean isImmutable = Boolean.FALSE
}
