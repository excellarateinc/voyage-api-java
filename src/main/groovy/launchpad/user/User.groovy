package launchpad.user

import com.fasterxml.jackson.annotation.JsonIgnore
import launchpad.role.Role
import org.hibernate.validator.constraints.Email

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
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
    @NotNull
    String email

    @NotNull
    String password

    @NotNull
    Boolean isEnabled

    @NotNull
    Boolean isAccountExpired

    @NotNull
    Boolean isAccountLocked

    @NotNull
    Boolean isCredentialsExpired

    @NotNull
    @JsonIgnore
    Boolean isDeleted

    @ManyToMany
    @JoinTable(name="user_role", joinColumns=@JoinColumn(name="user_id"), inverseJoinColumns=@JoinColumn(name="role_id"))
    @JsonIgnore
    Set<Role> roles
}
