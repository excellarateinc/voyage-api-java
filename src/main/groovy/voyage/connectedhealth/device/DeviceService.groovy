package voyage.connectedhealth.device

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voyage.connectedhealth.result.ResultTypeService
import voyage.connectedhealth.user.UserDevice

@Transactional
@Service
class DeviceService {
    private DeviceRepository deviceRepository
    private ResultTypeService resultTypeService

    @Autowired
    DeviceService(DeviceRepository deviceRepository,
                  ResultTypeService resultTypeService) {
        this.deviceRepository = deviceRepository
        this.resultTypeService = resultTypeService
    }

    Device get(long id) {
        return deviceRepository.findByIdAndIsDeletedFalse(id)
    }

    Page<Device> findByResultType(long resultTypeId, Pageable pageable) {
        return deviceRepository.findByResultTypeIdAndIsDeletedFalseOrderByName(resultTypeId, pageable)
    }

    Page<Device> findAll(Pageable pageable) {
        return deviceRepository.findByIsDeletedFalse(pageable)
    }

    Device save(Device deviceIn) {
        Device device
        if (deviceIn.id) {
            device = get(deviceIn.id)
        } else {
            device = new Device()
            device.resultType = resultTypeService.get(deviceIn.resultType.id)
        }
        device.with {
            name = deviceIn.name
            model = deviceIn.model
            logoUrl = deviceIn.logoUrl
        }
        deviceRepository.save(device)
    }

    void delete(Device deviceIn) {
        Device device = deviceRepository.findOne(deviceIn.id)
        device.isDeleted = true
        deviceRepository.save(device)
    }
}
