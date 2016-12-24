package launchpad.role

import com.fasterxml.jackson.annotation.JsonIgnore
import launchpad.permission.Permission

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
    Boolean isDeleted

    @ManyToMany
    @JoinTable(name="role_permission", joinColumns=@JoinColumn(name="role_id"), inverseJoinColumns=@JoinColumn(name="permission_id"))
    Set<Permission> permissions
}
