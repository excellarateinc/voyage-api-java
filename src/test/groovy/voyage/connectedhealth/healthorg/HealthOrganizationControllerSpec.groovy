package voyage.connectedhealth.healthorg

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import voyage.security.AuthenticatedIntegrationTest

class HealthOrganizationControllerSpec extends AuthenticatedIntegrationTest {

    def "findByCurrentUser - get the health organizations for the current user"() {
        when:
        ResponseEntity<List> responseEntity = GET('/connectedhealth/v1/healthorg', List, superClient)

        then:
        responseEntity.statusCode == HttpStatus.OK
        responseEntity.body.size() == 0
    }

    def "get - get a health organization"() {
        when:
        ResponseEntity<HealthOrganization> responseEntity = GET('/connectedhealth/v1/healthorg/1', HealthOrganization, superClient)

        then:
        responseEntity.statusCode == HttpStatus.OK
        responseEntity.body.name == 'Bassett Healthcare Networks'
        responseEntity.body.mainPhoneNumber == '123-1233-1333'
        responseEntity.body.city == 'Albany'
        responseEntity.body.state == 'NY'
    }

    def "findAll - find all active health organizations"() {
        when:
        ResponseEntity<List> responseEntity = GET('/connectedhealth/v1/healthorg/all', List, superClient)

        then:
        responseEntity.statusCode == HttpStatus.OK
        responseEntity.body.size() == 2
    }

    def "create - create a new health organization"() {
        given:
        HealthOrganization healthOrganization = new HealthOrganization(name: 'Test', mainPhoneNumber: '123',
        city: 'City', state: 'ST', logoUrl: '/')
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<HealthOrganization> httpEntity = new HttpEntity<HealthOrganization>(healthOrganization, headers)


        when:
        ResponseEntity<HealthOrganization> result = POST('/connectedhealth/v1/healthorg', httpEntity, HealthOrganization, superClient)

        then:
        result.statusCode == HttpStatus.CREATED
        result.body.name == 'Test'
        result.body.mainPhoneNumber == '123'
        result.body.city == 'City'
        result.body.state == 'ST'
        result.body.logoUrl == '/'
    }

    def "assign - assign a health organization to the current user"() {
        given:
        ResponseEntity<HealthOrganization> healthOrganizationEntity = GET('/connectedhealth/v1/healthorg/1', HealthOrganization, superClient)
        HealthOrganization healthOrganization = healthOrganizationEntity.getBody()
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<HealthOrganization> httpEntity = new HttpEntity<HealthOrganization>(healthOrganization, headers)

        when:
        ResponseEntity<HealthOrganizationUser> result = POST('/connectedhealth/v1/healthorg/assign', httpEntity, HealthOrganizationUser, superClient)

        then:
        result.statusCode == HttpStatus.CREATED
        result.body.healthOrganization.id == healthOrganization.id
        result.body.healthOrganization.name == healthOrganization.name
        result.body.healthOrganization.mainPhoneNumber == healthOrganization.mainPhoneNumber
        result.body.healthOrganization.city == healthOrganization.city
        result.body.healthOrganization.state == healthOrganization.state
        result.body.healthOrganization.logoUrl == healthOrganization.logoUrl
        result.body.user
        !result.body.isPrimary
        !result.body.isShareData
    }

    def "update - update a health organization"() {
        given:
        ResponseEntity<HealthOrganization> healthOrganizationEntity = GET('/connectedhealth/v1/healthorg/1', HealthOrganization, superClient)
        HealthOrganization healthOrganization = healthOrganizationEntity.getBody()
        healthOrganization.name = 'Updated'
        healthOrganization.mainPhoneNumber = 'Updated'
        healthOrganization.city = 'Updated'
        healthOrganization.state = 'UD'
        healthOrganization.logoUrl = 'Updated'
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<HealthOrganization> httpEntity = new HttpEntity<HealthOrganization>(healthOrganization, headers)

        when:
        ResponseEntity<HealthOrganization> result = PUT('/connectedhealth/v1/healthorg', httpEntity, HealthOrganization, superClient)

        then:
        result.statusCode == HttpStatus.NO_CONTENT
        ResponseEntity<HealthOrganization> updated = GET('/connectedhealth/v1/healthorg/1', HealthOrganization, superClient)
        updated.body.name == 'Updated'
        updated.body.mainPhoneNumber == 'Updated'
        updated.body.city == 'Updated'
        updated.body.state == 'UD'
        updated.body.logoUrl == 'Updated'
    }

    def "findAllUserSettings - find all user settings for the given user"() {
        when:
        ResponseEntity<List> responseEntity = GET('/connectedhealth/v1/healthorg/usersettings', List, superClient)

        then:
        responseEntity.statusCode == HttpStatus.OK
        responseEntity.body.size() == 0
    }

    def "updateUserSettings - update user settings for the given health organization"() {
        given:
        ResponseEntity<HealthOrganization> healthOrganizationEntity = GET('/connectedhealth/v1/healthorg/1', HealthOrganization, superClient)
        HealthOrganization healthOrganization = healthOrganizationEntity.getBody()
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<HealthOrganization> httpEntity = new HttpEntity<HealthOrganization>(healthOrganization, headers)

        when:
        ResponseEntity<HealthOrganizationUser> result = POST('/connectedhealth/v1/healthorg/assign', httpEntity, HealthOrganizationUser, superClient)

        then:
        result.statusCode == HttpStatus.CREATED

        when:
        HealthOrganizationUserSettings userSettings = new HealthOrganizationUserSettings()
        userSettings.isShareData = true
        userSettings.isPrimary = true
        userSettings.healthOrganization = healthOrganization
        HttpEntity<HealthOrganizationUserSettings> userSettingsHttpEntity = new HttpEntity<HealthOrganizationUserSettings>(userSettings, headers)
        ResponseEntity<HealthOrganizationUserSettings> userSettingsResponse =
                PUT('/connectedhealth/v1/healthorg/usersettings', userSettingsHttpEntity, HealthOrganizationUserSettings, superClient)

        then:
        userSettingsResponse.statusCode == HttpStatus.NO_CONTENT

        when:
        ResponseEntity<List> userSettingsResponse2 = GET('/connectedhealth/v1/healthorg/usersettings', List, superClient)

        then:
        userSettingsResponse2.statusCode == HttpStatus.OK
        userSettingsResponse2.body.size() == 1
    }
}
