package launchpad.service

import launchpad.common.service.BaseEntityService
import launchpad.domain.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service("userService")
class UserService extends  BaseEntityService {

    UserService() {
        super(User.class)
    }
}
