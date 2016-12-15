package launchpad.user

import groovy.json.JsonSlurper
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

/**
 * Created by dhanumandla on 14/12/16.
 */
class UserControllerSpec extends Specification {
    private User user
    def userService = Mock(UserService)
    def classUnderTest = new UserController(userService)

    def mockMvc = MockMvcBuilders.standaloneSetup(classUnderTest).build()

    def setup() {
        user = new User(id: 1, firstName: 'LSS', lastName: 'India')
    }
    def 'User list test hits the REST endpoint and parses the JSON output' () {
        when: 'rest user url is hit'
            def response = mockMvc.perform(get('/v1/users')).andReturn().response
            def content = new JsonSlurper().parseText(response.contentAsString)
        then: 'securityService correctly returned account in JSON'
            1 * userService.listAll() >> [user]
            //Testing the HTTP Status code
            response.status == OK.value()

            //Showing how a contains test could work
            response.contentType.contains('application/json')
            response.contentType == 'application/json;charset=UTF-8'

    }

}
