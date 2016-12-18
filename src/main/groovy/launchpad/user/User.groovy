package launchpad.user

import org.hibernate.validator.constraints.Email

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
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
}
