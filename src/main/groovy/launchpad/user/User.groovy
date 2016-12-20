package launchpad.user

import org.hibernate.validator.constraints.Email

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.UniqueConstraint
import javax.validation.constraints.NotNull
import javax.validation.constraints.Null

@Entity
class User {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id

    @NotNull
    String firstName

    @NotNull
    String lastName

    @NotNull
    String username

    @Email
    String email

    @Null
    String password

    @NotNull
    Boolean isEnabled

    @NotNull
    Boolean isAccountExpired

    @NotNull
    Boolean isAccountLocked

    @NotNull
    Boolean isCredentialsExpired

    @OneToMany(fetch = FetchType.LAZY, mappedBy = 'user')
    Set<UserRole> userRoles
}
