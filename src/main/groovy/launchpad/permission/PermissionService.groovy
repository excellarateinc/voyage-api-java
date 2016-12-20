package launchpad.permission

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
        permissionRepository.delete(id)
    }

    Permission get(@NotNull Long id) {
        return permissionRepository.findOne(id)
    }

    Iterable<Permission> listAll() {
        return permissionRepository.findAll()
    }

    Permission save(@Valid Permission permission) {
        return permissionRepository.save(permission)
    }

    Permission update(@Valid Permission permission) {
        return permissionRepository.save(permission)
    }

}
