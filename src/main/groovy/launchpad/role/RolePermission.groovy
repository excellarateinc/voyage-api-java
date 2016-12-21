package launchpad.role

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.EqualsAndHashCode
import launchpad.permission.Permission

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@EqualsAndHashCode(includes = 'authority')
@Entity
class RolePermission {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'role_id', nullable = false)
    @NotNull
    Role role

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'permission_id', nullable = false)
    @NotNull
    Permission permission

    @NotNull
    @JsonIgnore
    Boolean isDeleted
}
