package launchpad.permission

import launchpad.error.UnknownIdentifierException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Transactional
@Service('permissionService')
@Validated
class PermissionService {
    private final PermissionRepository permissionRepository

    PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository
    }

    void delete(@NotNull Long id) {
        Permission permission = get(id)
        permission.isDeleted = true
        save(permission)
    }

    Permission get(@NotNull Long id) {
        Permission permission = permissionRepository.findOne(id)
        if (!permission) {
            throw new UnknownIdentifierException()
        }
        return permission
    }

    Iterable<Permission> listAll() {
        return permissionRepository.findAll()
    }

    Permission save(@Valid Permission permission) {
        return permissionRepository.save(permission)
    }
}
