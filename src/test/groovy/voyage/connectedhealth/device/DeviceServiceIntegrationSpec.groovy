package voyage.connectedhealth.device

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import spock.lang.Specification
import voyage.connectedhealth.result.ResultType
import voyage.connectedhealth.result.ResultTypeService

import javax.transaction.Transactional

@SpringBootTest
class DeviceServiceIntegrationSpec extends Specification {

    @Autowired
    private DeviceService deviceService
    @Autowired
    private ResultTypeService resultTypeService

    @Transactional
    def 'save - save a new device'() {
        given:
        ResultType resultType = resultTypeService.get(1L)
        Device newDevice = new Device(
                name:'A Device Name', model:'ABC123'
        )
        newDevice.resultType = resultType

        when:
        Device saved = deviceService.save(newDevice)

        then:
        saved.id
        saved.name == 'A Device Name'
        saved.model == 'ABC123'
    }

    def 'findByResultType - find all devices by result type'() {
        when:
        Page<Device> devices = deviceService.findByResultType(1L, new PageRequest(0, 10))

        then:
        devices.size() == 1
    }

    def 'findAll - find all devices sort by name'() {
        when:
        Pageable pageable = new PageRequest(0, 10, new Sort(Sort.Direction.DESC, 'name'))
        Page<Device> devices = deviceService.findAll(pageable)

        then:
        devices.size() == 3
        devices[0].name == 'Spiro Pro'
        devices[1].name == 'Glucose Pro'
        devices[2].name == 'Coag-Sense Pro'
    }

    def 'get - get device by ID'() {
        when:
        Device result = deviceService.get(1L)

        then:
        result.id == 1L
        result.name == 'Coag-Sense Pro'
        result.model == 'ABC-1'
        result.logoUrl == '/'
        !result.isDeleted
    }

    def 'delete - soft delete a device'() {
        given:
        Device device = deviceService.get(1L)

        when:
        deviceService.delete(device)

        then:
        !deviceService.get(1L)
    }
}
