package voyage.security.user

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.envers.Audited
import org.hibernate.validator.constraints.Email
import voyage.common.AuditableEntity
import voyage.security.role.Role

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.validation.constraints.NotNull

@Entity
@Audited
class User extends AuditableEntity {
    @NotNull
    String firstName

    @NotNull
    String lastName

    @NotNull
    String username

    @Email
    @NotNull
    String email

    @NotNull
    String password

    @NotNull
    Boolean isEnabled = Boolean.TRUE

    @NotNull
    Boolean isAccountExpired = Boolean.FALSE

    @NotNull
    Boolean isAccountLocked = Boolean.FALSE

    @NotNull
    Boolean isCredentialsExpired = Boolean.FALSE

    @NotNull
    @JsonIgnore
    Boolean isVerifyRequired = Boolean.FALSE

    @ManyToMany
    @JoinTable(name='user_role', joinColumns=@JoinColumn(name='user_id'), inverseJoinColumns=@JoinColumn(name='role_id'))
    @JsonIgnore
    Set<Role> roles

    @OneToMany(fetch=FetchType.EAGER, mappedBy='user')
    Set<UserPhone> phones

    @JsonIgnore
    String verifyCode

    @JsonIgnore
    Date verifyCodeExpiresOn

    boolean isVerifyCodeExpired() {
        return verifyCodeExpiresOn != null && verifyCodeExpiresOn < new Date()
    }

    String getMaskedEmail() {
        return email?.replaceAll('(?<=.{2}).(?=.*@)', '*')
    }
}
