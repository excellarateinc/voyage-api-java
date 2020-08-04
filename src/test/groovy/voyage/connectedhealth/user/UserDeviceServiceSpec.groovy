package voyage.connectedhealth.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import voyage.connectedhealth.device.Device
import voyage.connectedhealth.device.DeviceService
import voyage.security.TestAuthentication
import voyage.security.TestSecurityContext
import voyage.security.user.User
import voyage.security.user.UserService

@SpringBootTest
class UserDeviceServiceSpec extends Specification {
    @Autowired
    private UserDeviceService userDeviceService
    @Autowired
    private DeviceService deviceService
    @Autowired
    private UserService userService

    private TestAuthentication testAuthentication = new TestAuthentication(principal: 'super')

    def setup() {
        SecurityContextHolder.setContext(new TestSecurityContext(authentication: testAuthentication))
    }

    def 'get - get user device by ID'() {
        when:
        UserDevice result = userDeviceService.get(1L)

        then:
        result.id == 1L
        result.user.username == 'super'
        result.device.name == 'Coag-Sense Pro'
        result.serialNumber == '12223345566'
        result.expirationDate
    }

    def 'findAll - find all user devices by user'() {
        given:
        User user = userService.get(1L)

        when:
        List<UserDevice> results = userDeviceService.findAll(user)

        then:
        results.size() == 3
    }

    def 'save - save a new device'() {
        given:
        User user = userService.findByUsername('super')
        Device device = deviceService.get(1L)
        Date expirationDate = new Date()
        UserDevice userDevice = new UserDevice(
                device: new Device(id: 1L), serialNumber: 'ABC123', expirationDate: expirationDate
        )

        when:
        UserDevice result = userDeviceService.save(userDevice)

        then:
        result.id
        result.user == user
        result.device == device
        result.serialNumber == 'ABC123'
        result.expirationDate == expirationDate
    }

    def 'delete - soft delete a user device'() {
        given:
        UserDevice userDevice = userDeviceService.get(1L)

        when:
        userDeviceService.delete(userDevice)

        then:
        !userDeviceService.get(1L)
    }
}
