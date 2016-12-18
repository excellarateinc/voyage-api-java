package launchpad.user

import groovy.json.JsonSlurper
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

class UserControllerIntegrationSpec extends Specification {
    User user
    User modifiedUser
    MockMvc mockMvc
    UserService userService = Mock(UserService)
    UserController userController = new UserController(userService)

    def setup() {
        user = new User(id: 1, firstName: 'LSS', lastName: 'India')
        modifiedUser = new User(id: 1, firstName: 'LSS', lastName: 'Inc')
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build()
    }

    def 'User list test hits the REST endpoint and parses the JSON output' () {
        when: 'consume the REST URL to retrieve all Users'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.get(new URI("/v1/users")))
                    .andReturn().response
            def content = new JsonSlurper().parseText(response.contentAsString)
        then: 'verify the HTTP response'
            1 * userService.listAll() >> [user]
            response.status == HttpStatus.OK.value()
            response.contentType == MediaType.APPLICATION_JSON_UTF8_VALUE
            content[0].firstName == 'LSS'
            content[0].lastName == 'India'
    }

    def "User 'get' test hits the REST endpoint and parses the JSON output" () {
        when: 'consume the REST URL to retrieve a specific User'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.get(new URI("/v1/users/1")))
                    .andReturn().response
            def content = new JsonSlurper().parseText(response.contentAsString)
        then: 'verify the HTTP response'
            1 * userService.get(1) >> user
            response.status == HttpStatus.OK.value()
            response.contentType == MediaType.APPLICATION_JSON_UTF8_VALUE
            content.firstName == 'LSS'
            content.lastName == 'India'
    }

    def "User 'delete' test hits the REST endpoint and parses the JSON output" () {
        when: 'consume the REST URL to delete a specific User'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.delete(new URI("/v1/users/1")))
                    .andReturn().response
        then: 'verify the HTTP response'
            1 * userService.delete(1)
            response.status == HttpStatus.OK.value()
    }

    def "User 'create' test hits the REST endpoint and parses the JSON output" () {
        when: 'consume the REST URL to create a new User'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.post(new URI("/v1/users"))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content('{"lastName": "User", "firstName": "Test"}'))
                    .andReturn().response
            def content = new JsonSlurper().parseText(response.contentAsString)
        then: 'verify the HTTP response'
            1 * userService.save(_) >> {new User(id: 1, firstName: 'Test', lastName: 'User')}
            response.status == HttpStatus.OK.value()
            response.contentType == MediaType.APPLICATION_JSON_UTF8_VALUE
            content.firstName == 'Test'
            content.lastName == 'User'
    }

    def "User 'update' test hits the REST endpoint and parses the JSON output" () {
        when: 'consume the REST URL to update a User'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.put(new URI("/v1/users/1"))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content('{"id": 1, "lastName": "User1", "firstName": "Test1"}'))
                    .andReturn().response
            def content = new JsonSlurper().parseText(response.contentAsString)
        then: 'verify the HTTP response'
            1 * userService.update(_) >> {new User(id: 1, firstName: 'Test1', lastName: 'User1')}
            response.status == HttpStatus.OK.value()
            response.contentType == MediaType.APPLICATION_JSON_UTF8_VALUE
            content.firstName == 'Test1'
            content.lastName == 'User1'
    }
}
