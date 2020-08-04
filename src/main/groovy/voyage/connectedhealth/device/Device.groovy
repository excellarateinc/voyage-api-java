package voyage.connectedhealth.device


import groovy.transform.EqualsAndHashCode
import org.hibernate.sql.Update
import voyage.connectedhealth.result.ResultType
import voyage.connectedhealth.validation.Create
import voyage.security.audit.AuditableEntity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import javax.validation.constraints.Null

@Entity
@EqualsAndHashCode(callSuper=true)
class Device extends AuditableEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @NotNull(groups = Update.class)
    @Null(groups = Create.class)
    Long id

    @NotNull(groups = [Create.class, Update.class])
    @ManyToOne
    @JoinColumn(name='result_type_id')
    ResultType resultType

    @NotNull(groups = [Create.class, Update.class])
    String name

    @NotNull(groups = [Create.class, Update.class])
    String model

    String logoUrl
}
