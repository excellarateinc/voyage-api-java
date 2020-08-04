package voyage.connectedhealth.device


import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import voyage.connectedhealth.result.ResultType
import voyage.security.AuthenticatedIntegrationTest

class DeviceControllerIntegrationSpec extends AuthenticatedIntegrationTest {
    def "get - get a device"() {
        when:
        ResponseEntity<Device> responseEntity = GET('/connectedhealth/v1/device/1', Device, superClient)

        then:
        responseEntity.statusCode == HttpStatus.OK
        responseEntity.body.name == 'Coag-Sense Pro'
        responseEntity.body.model == 'ABC-1'
        responseEntity.body.logoUrl == '/'
    }

    def "findByResultType"() {
        when:
        ResponseEntity<List> responseEntity = GET('/connectedhealth/v1/device/type/1', List, superClient)

        then:
        responseEntity.statusCode == HttpStatus.OK
        responseEntity.body.size() == 1
    }

    def "/connectedhealth/v1/device/create POST - Create a new device"() {
        given:
        ResultType resultType = new ResultType(id: 1L)
        Device device = new Device(resultType: resultType, name: 'My Device', model: 'ABC001')
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<Device> httpEntity = new HttpEntity<Device>(device, headers)

        when:
        ResponseEntity<Device> responseEntity = POST('/connectedhealth/v1/device', httpEntity, Device, superClient)

        then:
        responseEntity.statusCode == HttpStatus.CREATED
        responseEntity.body.name == 'My Device'
    }

    def "update - update a Device"() {
        given:
        ResponseEntity<Device> responseEntity = GET('/connectedhealth/v1/device/1', Device, superClient)
        Device device = responseEntity.body
        device.name = 'New Device Name'
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<Device> httpEntity = new HttpEntity<Device>(device, headers)

        when:
        ResponseEntity<Device> response = PUT('/connectedhealth/v1/device', httpEntity, Device, superClient)

        then:
        response.statusCode == HttpStatus.OK
        response.body.name == 'New Device Name'
    }
}
