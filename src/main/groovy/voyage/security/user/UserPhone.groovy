package voyage.security.user

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.EqualsAndHashCode
import org.hibernate.envers.Audited
import voyage.common.AuditableEntity

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Entity
@Audited
@EqualsAndHashCode(includes=['phoneNumber'], callSuper=true)
class UserPhone extends AuditableEntity {
    @Enumerated(EnumType.STRING)
    PhoneType phoneType

    @NotNull
    String phoneNumber

    @JsonIgnore
    String verifyCode

    @NotNull
    @JsonIgnore
    Boolean isValidated = Boolean.FALSE

    @JsonIgnore
    Date verifyCodeExpiresOn

    @ManyToOne
    @JoinColumn(name='user_id')
    @JsonIgnore
    User user

    @JsonIgnore
    boolean isVerifyCodeExpired() {
        return verifyCodeExpiresOn != null && verifyCodeExpiresOn < new Date()
    }
}
