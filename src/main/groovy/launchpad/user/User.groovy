package launchpad.user

import org.hibernate.validator.constraints.Email

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.validation.constraints.NotNull

@Entity
class User {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id

    @NotNull
    String firstName

    @NotNull
    String lastName

    @Email
    String email

    @OneToMany(fetch = FetchType.LAZY, mappedBy = 'user')
    Set<UserRole> userRoles
}
