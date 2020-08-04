package voyage.connectedhealth.result

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import voyage.connectedhealth.healthorg.HealthOrganization
import voyage.connectedhealth.healthorg.HealthOrganizationService
import voyage.connectedhealth.user.UserDevice
import voyage.connectedhealth.user.UserDeviceService
import voyage.security.TestAuthentication
import voyage.security.TestSecurityContext
import voyage.security.user.User
import voyage.security.user.UserService

@SpringBootTest
class ResultServiceIntegrationSpec extends Specification {
    @Autowired
    private ResultService resultService
    @Autowired
    private UserService userService
    @Autowired
    private HealthOrganizationService healthOrganizationService
    @Autowired
    private ResultTypeService resultTypeService
    @Autowired
    private UserDeviceService userDeviceService

    private TestAuthentication testAuthentication = new TestAuthentication(principal: 'super')

    def setup() {
        SecurityContextHolder.setContext(new TestSecurityContext(authentication: testAuthentication))
    }

    def 'get - get a result by ID'() {
        when:
        Result result = resultService.get(1L)

        then:
        result.id == 1L
        result.user.id == 1L
        result.resultType.id == 1L
        result.healthOrganization.id == 1L
        result.userDevice.id == 1L
        result.value == 2.5
        result.resultMethod == ResultMethod.LAB
        result.entryDate
        result.comment == 'Kept to the same diet with no changes.  Stopped alcohol entirely for the week.'
        result.isValid
        !result.isDeleted
    }

    def "findRecent - find all results for the given user created recently"() {
        given:
        User user = userService.get(1L)
        PageRequest pageable = new PageRequest(0, 2)

        when:
        Page<Result> results = resultService.findRecent(user, pageable)

        then:
        results.size == 2
    }

    def "findRecent - find all results for the given user and health org created recently"() {
        given:
        User user = userService.get(1L)
        List<HealthOrganization> healthOrganizations = healthOrganizationService.findByUser(user)
        PageRequest pageable = new PageRequest(0, 2)

        when:
        Page<Result> results = resultService.findRecent(user, healthOrganizations[0], pageable)

        then:
        results.size == 2
    }

    def 'findAll - find all by user'() {
        given:
        User user = userService.get(1L)

        when:
        Page<Result> results = resultService.findAll(user, new PageRequest(0, 10))

        then:
        results.size() == 2
    }

    def 'findAll - find all by user and health organization'() {
        given:
        User user = userService.get(1L)
        HealthOrganization healthOrganization = healthOrganizationService.get(1L)

        when:
        Page<Result> results = resultService.findAll(user, healthOrganization, new PageRequest(0, 10))

        then:
        results.size() == 2
    }

    def 'findAll - find all by user and health organization and result type'() {
        given:
        User user = userService.get(1L)
        HealthOrganization healthOrganization = healthOrganizationService.get(1L)
        ResultType resultType = resultTypeService.get(1L)

        when:
        Page<Result> results = resultService.findAll(user, healthOrganization, resultType, new PageRequest(0, 10))

        then:
        results.size() == 1
    }

    def 'findAll - find all by user and result type'() {
        given:
        User user = userService.get(1L)
        ResultType resultType = resultTypeService.get(1L)

        when:
        Page<Result> results = resultService.findAll(user, resultType, new PageRequest(0, 10))

        then:
        results.size() == 1
    }

    def 'save - save a new result'() {
        given:
        User user = userService.findByUsername('super')
        HealthOrganization healthOrganization = healthOrganizationService.get(1L)
        ResultType resultType = resultTypeService.get(1L)
        UserDevice userDevice = userDeviceService.get(1L)
        Result result = new Result(resultType: new ResultType(id: 1L), healthOrganization: new HealthOrganization(id: 1L),
        userDevice: new UserDevice(id: 1L), value: 1, resultMethod: ResultMethod.SELF, entryDate: new Date(), comment: 'test')

        when:
        Result saved = resultService.save(result)

        then:
        saved.user == user
        saved.healthOrganization == healthOrganization
        saved.resultType == resultType
        saved.userDevice == userDevice
        saved.value == 1
        saved.resultMethod == ResultMethod.SELF
        saved.comment == 'test'
        saved.isValid
        !saved.isDeleted
    }
}
