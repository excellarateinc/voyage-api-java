package launchpad.user

import groovy.transform.EqualsAndHashCode
import org.hibernate.validator.constraints.Email

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.NotNull

@EqualsAndHashCode(includes = "authority")
@Entity
class Role {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id

    @NotNull
    String name

    @NotNull
    String authority
}
