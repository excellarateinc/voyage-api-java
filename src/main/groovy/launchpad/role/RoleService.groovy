package launchpad.role

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

    RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository
    }

    void delete(@NotNull Long id) {
        roleRepository.delete(id)
    }

    Role get(@NotNull Long id) {
        return roleRepository.findOne(id)
    }

    Iterable<Role> listAll() {
        return roleRepository.findAll()
    }

    Role save(@Valid Role role) {
        return roleRepository.save(role)
    }

    Role update(@Valid Role role) {
        return roleRepository.save(role)
    }

}
