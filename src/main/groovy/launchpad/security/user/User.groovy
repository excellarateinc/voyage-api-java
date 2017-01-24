package launchpad.security.user

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.EqualsAndHashCode
import launchpad.security.client.ClientRedirectUri
import launchpad.security.role.Role
import org.hibernate.validator.constraints.Email

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
@EqualsAndHashCode
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

    @NotNull
    @JsonIgnore
    Boolean isDeleted = Boolean.FALSE

    @ManyToMany
    @JoinTable(name='user_role', joinColumns=@JoinColumn(name='user_id'), inverseJoinColumns=@JoinColumn(name='role_id'))
    @JsonIgnore
    Set<Role> roles

    @JsonIgnore
    String verifyCode

    @JsonIgnore
    Date verifyCodeExpiresOn

    @OneToMany(fetch=FetchType.EAGER, mappedBy='user')
    @JsonIgnore
    Set<UserPhone> userPhones

    boolean isVerifyCodeExpired() {
        return verifyCodeExpiresOn != null && verifyCodeExpiresOn < new Date()
    }
}
