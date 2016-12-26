package launchpad.security.role

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.EqualsAndHashCode
import launchpad.security.permission.Permission

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.validation.constraints.NotNull

@Entity
@EqualsAndHashCode
class Role {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id

    @NotNull
    String name

    @NotNull
    String authority

    @NotNull
    @JsonIgnore
    Boolean isDeleted = Boolean.FALSE

    @ManyToMany
    @JoinTable(name='role_permission', joinColumns=@JoinColumn(name='role_id'), inverseJoinColumns=@JoinColumn(name='permission_id'))
    @JsonIgnore
    Set<Permission> permissions
}
