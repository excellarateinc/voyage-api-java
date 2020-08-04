package voyage.connectedhealth.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import voyage.connectedhealth.device.DeviceService
import voyage.security.user.User
import voyage.security.user.UserService

import javax.transaction.Transactional

@Transactional
@Service
class UserDeviceService {
    private UserDeviceRepository userDeviceRepository
    private UserService userService
    private DeviceService deviceService

    @Autowired
    UserDeviceService(UserDeviceRepository userDeviceRepository,
                      UserService userService,
                      DeviceService deviceService) {
        this.userDeviceRepository = userDeviceRepository
        this.userService = userService
        this.deviceService = deviceService
    }

    UserDevice get(long userDeviceId) {
        return userDeviceRepository.findByIdAndIsDeletedFalse(userDeviceId)
    }

    List<UserDevice> findAll(User user) {
        return userDeviceRepository.findByUserAndIsDeletedFalse(user)
    }

    UserDevice save(UserDevice userDeviceIn) {
        UserDevice userDevice
        if (userDeviceIn.id) {
            userDevice = get(userDeviceIn.id)
        } else {
            userDevice = new UserDevice()
            userDevice.user = userService.currentUser
            userDevice.device = deviceService.get(userDeviceIn.device.id)
        }
        userDevice.with {
            serialNumber = userDeviceIn.serialNumber
            expirationDate = userDeviceIn.expirationDate
        }
        return userDeviceRepository.save(userDevice)
    }

    void delete(UserDevice userDeviceIn) {
        UserDevice userDevice = userDeviceRepository.findOne(userDeviceIn.id)
        userDevice.isDeleted = true
        userDeviceRepository.save(userDevice)
    }
}
