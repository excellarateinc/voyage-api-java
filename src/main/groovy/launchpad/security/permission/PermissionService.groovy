package launchpad.security.permission

import launchpad.error.ImmutableRecordException
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
        permissionRepository.save(permission)
    }

    Permission findByName(@NotNull String name) {
        Permission permission = permissionRepository.findByName(name)
        if (!permission) {
            throw new UnknownIdentifierException("Unknown permission name given: ${name}")
        }
        return permission
    }

    Permission findByName(@NotNull String name) {
        Permission permission = permissionRepository.findByName(name)
        if (!permission) {
            throw new UnknownIdentifierException("Unknown permission name given: ${permissionName}")
        }
        return permission
    }

    Iterable<Permission> findAllByUser(@NotNull Long userId) {
        permissionRepository.findAllByUserId(userId)
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
        if (permission.id) {
            Permission existingPermission = get(permission.id)
            if (existingPermission.isImmutable) {
                throw new ImmutableRecordException()
            }
        }
        return permissionRepository.save(permission)
    }
}
