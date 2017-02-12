package voyage.security.permission

import voyage.common.AuditableEntity

import javax.persistence.Entity
import javax.validation.constraints.NotNull

@Entity
class Permission extends AuditableEntity {
    @NotNull
    String name

    String description

    @NotNull
    Boolean isImmutable = Boolean.FALSE
}
