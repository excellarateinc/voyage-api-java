package voyage.connectedhealth.device

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import voyage.connectedhealth.validation.Create
import voyage.connectedhealth.validation.Update
import voyage.security.user.User

@RestController
@RequestMapping(['/connectedhealth/v1/device', '/connectedhealth/v1.0/device'])
class DeviceController {
    private final DeviceService deviceService

    @Autowired
    DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService
    }

    @GetMapping('/{deviceId}')
//    @PreAuthorize("hasAuthority('')")
    ResponseEntity get(@PathVariable('deviceId') Long deviceId) {
        Device device = deviceService.get(deviceId)
        return new ResponseEntity(device, HttpStatus.OK)
    }

    @GetMapping()
//    @PreAuthorize("hasAuthority('')")
    ResponseEntity findAll(Pageable pageable) {
        Page<Device> devices = deviceService.findAll(pageable)
        return new ResponseEntity(devices.getContent(), HttpStatus.OK)
    }

    @GetMapping('/type/{resultTypeId}')
//    @PreAuthorize("hasAuthority('')")
    ResponseEntity findByResultType(@PathVariable('resultTypeId') Long resultTypeId, Pageable pageable) {
        Page<Device> devices = deviceService.findByResultType(resultTypeId, pageable)
        return new ResponseEntity(devices.getContent(), HttpStatus.OK)
    }

    @PostMapping
//    @PreAuthorize("hasAuthority('')")
    ResponseEntity create(@Validated(Create.class) @RequestBody Device deviceIn) {
        Device device = deviceService.save(deviceIn)
        return new ResponseEntity(device, HttpStatus.CREATED)
    }

    @PutMapping
//    @PreAuthorize("hasAuthority('')")
    ResponseEntity update(@Validated(Update.class) @RequestBody Device deviceIn) {
        Device device = deviceService.save(deviceIn)
        return new ResponseEntity(device, HttpStatus.OK)
    }

    @DeleteMapping
//    @PreAuthorize("hasAuthority('')")
    ResponseEntity delete(@Validated(Update.class) @RequestBody Device deviceIn) {
        deviceService.delete(deviceIn)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
