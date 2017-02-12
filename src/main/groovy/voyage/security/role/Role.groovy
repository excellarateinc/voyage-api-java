package voyage.security.role

import com.fasterxml.jackson.annotation.JsonIgnore
import voyage.common.AuditableEntity
import voyage.security.permission.Permission

import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.validation.constraints.NotNull

@Entity
class Role extends AuditableEntity {
    @NotNull
    String name

    @NotNull
    String authority
    
    @ManyToMany
    @JoinTable(name='role_permission', joinColumns=@JoinColumn(name='role_id'), inverseJoinColumns=@JoinColumn(name='permission_id'))
    @JsonIgnore
    Set<Permission> permissions
}
