package launchpad.security.permission

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.EqualsAndHashCode

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity
@EqualsAndHashCode
class Permission {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id

    @NotNull
    String name

    String description

    @NotNull
    Boolean isImmutable = Boolean.FALSE

    @NotNull
    @JsonIgnore
    Boolean isDeleted = Boolean.FALSE
}
