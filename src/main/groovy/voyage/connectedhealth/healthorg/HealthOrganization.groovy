package voyage.connectedhealth.healthorg

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.EqualsAndHashCode
import org.hibernate.sql.Update
import voyage.connectedhealth.validation.Create
import voyage.security.audit.AuditableEntity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.NotNull
import javax.validation.constraints.Null

@Entity
@EqualsAndHashCode(callSuper=true)
class HealthOrganization extends AuditableEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @NotNull(groups = Update.class)
    @Null(groups = Create.class)
    Long id

    @NotNull(groups = [Create.class, Update.class])
    String name

    @NotNull(groups = [Create.class, Update.class])
    String mainPhoneNumber

    String city

    String state

    String logoUrl

    @NotNull(groups = [Create.class, Update.class])
    @JsonIgnore
    Boolean isEnabled = Boolean.TRUE


}
