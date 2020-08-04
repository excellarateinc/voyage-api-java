package voyage.connectedhealth.healthorg

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import spock.lang.Specification
import voyage.security.user.User
import voyage.security.user.UserService

@SpringBootTest
class HealthOrganizationServiceIntegrationSpec extends Specification {
    @Autowired
    HealthOrganizationService healthOrganizationService
    @Autowired
    UserService userService

    def 'findByUser - find all health organizations for the given user'() {
        given:
        User user = userService.findByUsername('super')

        when:
        List<HealthOrganization> healthOrganizations = healthOrganizationService.findByUser(user)

        then:
        healthOrganizations.size() == 2
    }

    def 'findAll - find all active health organizations'() {
        when:
        Page<HealthOrganization> healthOrganizations = healthOrganizationService.findAll(new PageRequest(0, 10))

        then:
        healthOrganizations.size() == 2
    }

    def 'get - get a health organization by ID'() {
        when:
        HealthOrganization result = healthOrganizationService.get(1L)

        then:
        result.id == 1L
        result.name == 'Bassett Healthcare Networks'
        result.mainPhoneNumber == '123-1233-1333'
        result.city == 'Albany'
        result.state == 'NY'
        result.logoUrl == '/logo/basset-healthcare-networks.ico'
    }
}
