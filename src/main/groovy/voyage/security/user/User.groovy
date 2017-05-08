package voyage.security.user

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.EqualsAndHashCode
import org.hibernate.annotations.Where
import org.hibernate.envers.Audited
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotBlank
import voyage.common.AuditableEntity
import voyage.security.role.Role

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.validation.constraints.NotNull

@Entity
@Audited
@EqualsAndHashCode(includes=['firstName', 'lastName', 'username'], callSuper=true)
class User extends AuditableEntity {
    @NotBlank
    String firstName

    @NotBlank
    String lastName

    @NotBlank
    String username

    @Email
    String email

    @NotBlank
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

    @JsonIgnore
    Integer failedLoginAttempts

    /**
     * Force all tokens for this client created on or before this date to be expired even if the original token has not
     * yet expired.
     */
    @JsonIgnore
    Date forceTokensExpiredDate

    @ManyToMany
    @JoinTable(name='user_role', joinColumns=@JoinColumn(name='user_id'), inverseJoinColumns=@JoinColumn(name='role_id'))
    @JsonIgnore
    Set<Role> roles

    @OneToMany(fetch=FetchType.EAGER, mappedBy='user', cascade=CascadeType.ALL)
    @Where(clause = 'is_deleted = 0')
    Set<UserPhone> phones
}
