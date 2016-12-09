package com.lighthousesoftware.launchpad.domain

import com.lighthousesoftware.launchpad.common.domain.BaseEntity
import grails.persistence.Entity

@Entity
class User extends BaseEntity<User> {

    String firstName
    String lastName

    static constraints = {
        firstName blank:false
        lastName blank:false
    }
}
