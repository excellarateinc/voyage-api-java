package launchpad.user

import org.springframework.data.repository.CrudRepository

interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username)
}
