package com.lighthousesoftware.launchpad.service

import com.lighthousesoftware.launchpad.common.service.BaseEntityService
import com.lighthousesoftware.launchpad.domain.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service("userService")
class UserService extends  BaseEntityService {

    UserService() {
        super(User.class)
    }
}
