package launchpad.role

import launchpad.error.UnknownIdentifierException
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
        Role role = get(id)
        role.isDeleted = true
        save(role)
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
}
