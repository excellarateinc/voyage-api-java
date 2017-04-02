package voyage.security.permission

import voyage.common.error.ImmutableRecordException
import voyage.common.error.UnknownIdentifierException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Service
@Transactional
@Validated
class PermissionService {
    private final PermissionRepository permissionRepository

    PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository
    }

    void delete(@NotNull Long id) {
        Permission permission = get(id)
        if (permission.isImmutable) {
            throw new ImmutableRecordException()
        }
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

    Iterable<Permission> findAllByUser(@NotNull Long userId) {
        permissionRepository.findAllByUserId(userId)
    }

    Iterable<Permission> findAllByClient(@NotNull Long clientId) {
        permissionRepository.findAllByClientId(clientId)
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

    Permission saveDetached(@Valid Permission permission) {
        if (permission.id) {
            Permission existingPermission = get(permission.id)
            if (existingPermission.isImmutable) {
                throw new ImmutableRecordException()
            }
            existingPermission.with {
                name = permission.name
                description = permission.description
                isDeleted = permission.isDeleted
            }
            return permissionRepository.save(existingPermission)
        }
        return permissionRepository.save(permission)
    }
}
