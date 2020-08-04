package voyage.connectedhealth.user

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.EqualsAndHashCode
import org.hibernate.sql.Update
import voyage.connectedhealth.device.Device
import voyage.connectedhealth.validation.Create
import voyage.security.audit.AuditableEntity
import voyage.security.user.User

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
class UserDevice extends AuditableEntity {
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
    @JoinColumn(name='device_id')
    Device device

    @NotNull
    String serialNumber

    Date expirationDate
}
