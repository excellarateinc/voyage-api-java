package launchpad.security.role

import launchpad.error.UnknownIdentifierException
import launchpad.security.permission.Permission
import launchpad.security.permission.PermissionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Transactional
@Service('roleService')
@Validated
class RoleService {
    private final RoleRepository roleRepository
    private final PermissionService permissionService

    RoleService(RoleRepository roleRepository, PermissionService permissionService) {
        this.roleRepository = roleRepository
        this.permissionService = permissionService
    }

    void delete(@NotNull Long id) {
        Role role = get(id)
        role.isDeleted = true
        roleRepository.save(role)
    }

    Role get(@NotNull Long id) {
        Role role = roleRepository.findOne(id)
        if (!role) {
            throw new UnknownIdentifierException()
        }
        return role
    }

    Iterable<Role> listAll() {
        return roleRepository.findAll()
    }

    Role save(@Valid Role role) {
        if (role.id) {
            get(role.id)
        }
        return roleRepository.save(role)
    }

    /**
     * Used by integration tests to quickly add a permission to validate that access is allowing/denying based on the
     * permission name.
     */
    void addPermission(@NotNull Long roleId, @NotNull String permissionName) {
        Permission permission = permissionService.findByName(permissionName)
        Role role = get(roleId)
        role.permissions.add(permission)
        save(role)
    }
}
