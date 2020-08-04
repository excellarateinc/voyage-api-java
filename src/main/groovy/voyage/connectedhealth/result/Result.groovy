package voyage.connectedhealth.result

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.EqualsAndHashCode
import org.hibernate.sql.Update
import voyage.connectedhealth.healthorg.HealthOrganization
import voyage.connectedhealth.user.UserDevice
import voyage.connectedhealth.validation.Create
import voyage.security.audit.AuditableEntity
import voyage.security.user.User

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import javax.validation.constraints.Null

@Entity
@EqualsAndHashCode(callSuper=true)
class Result extends AuditableEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @NotNull(groups = Update.class)
    @Null(groups = Create.class)
    Long id

    @NotNull
    @ManyToOne
    @JoinColumn(name='user_id')
    @JsonIgnore
    User user

    @NotNull
    @ManyToOne
    @JoinColumn(name='health_organization_id')
    @JsonIgnore
    HealthOrganization healthOrganization

    @NotNull
    @Enumerated(EnumType.STRING)
    ResultMethod resultMethod

    @NotNull
    @ManyToOne
    @JoinColumn(name='result_type_id')
    ResultType resultType

    @ManyToOne
    @JoinColumn(name='user_device_id')
    UserDevice userDevice

    @NotNull
    BigDecimal value

    @NotNull
    Date entryDate

    String comment

    @NotNull
    Boolean isValid = Boolean.TRUE
}
