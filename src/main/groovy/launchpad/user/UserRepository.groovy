package launchpad.user

import org.springframework.data.repository.CrudRepository

/**
 * Spring data repository class which extends the CRUD Repository class to re-use the CRUD functionality
 * for the User entity.
 */
interface UserRepository extends CrudRepository<User, Long> {

}
