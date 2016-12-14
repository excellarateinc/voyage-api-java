package launchpad.user

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

/**
 * Entity class to represent a user of the system with attributes such as first-name and last-name.
 */
@Entity
class User {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id
    String firstName
    String lastName

}
