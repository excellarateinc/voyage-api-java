package launchpad.domain

import grails.persistence.Entity
import launchpad.common.domain.BaseEntity

@Entity
class User extends BaseEntity<User> {

    String firstName
    String lastName

    static constraints = {
        firstName blank:false
        lastName blank:false
    }
}
