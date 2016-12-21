package launchpad.role

import groovy.json.JsonSlurper
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

class RoleControllerIntegrationSpec extends Specification {
    Role role
    MockMvc mockMvc
    RoleService roleService = Mock(RoleService)
    RoleController roleController = new RoleController(roleService)

    def setup() {
        role = new Role(id:1, name:'Super User', authority: 'ROLE_SUPER')
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build()
    }

    def 'Role list test hits the REST endpoint and parses the JSON output'() {
        when: 'consume the REST URL to retrieve all Roles'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.get(new URI('/v1/roles')))
                    .andReturn().response
            def content = new JsonSlurper().parseText(response.contentAsString)
        then: 'verify the HTTP response'
            1 * roleService.listAll() >> [role]
            HttpStatus.OK.value() == response.status
            MediaType.APPLICATION_JSON_UTF8_VALUE == response.contentType
            'Super User' == content[0].name
            'ROLE_SUPER' ==  content[0].authority
    }

    def "Role 'get' test hits the REST endpoint and parses the JSON output"() {
        when: 'consume the REST URL to retrieve a specific Role'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.get(new URI('/v1/roles/1')))
                    .andReturn().response
            def content = new JsonSlurper().parseText(response.contentAsString)
        then: 'verify the HTTP response'
            1 * roleService.get(1) >> role
            HttpStatus.OK.value() == response.status
            MediaType.APPLICATION_JSON_UTF8_VALUE == response.contentType
            'Super User' == content.name
            'ROLE_SUPER' ==  content.authority
    }

    def "Role 'delete' test hits the REST endpoint and parses the JSON output"() {
        when: 'consume the REST URL to delete a specific Role'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.delete(new URI('/v1/roles/1')))
                    .andReturn().response
        then: 'verify the HTTP response'
            1 * roleService.delete(1)
            HttpStatus.NO_CONTENT.value() == response.status
    }

    def "Role 'create' test hits the REST endpoint and parses the JSON output"() {
        when: 'consume the REST URL to create a new Role'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.post(new URI('/v1/roles'))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content('{"name": "Admin", "authority": "ADMIN"}'))
                    .andReturn().response
            def content = new JsonSlurper().parseText(response.contentAsString)
        then: 'verify the HTTP response'
            1 * roleService.save(_) >> { new Role(id:1, name:'Admin', authority:'ADMIN') }
            HttpStatus.CREATED.value() == response.status
            MediaType.APPLICATION_JSON_UTF8_VALUE == response.contentType
            '/v1/roles/1' == response.getHeaderValue(HttpHeaders.LOCATION)
            'Admin' == content.name
            'ADMIN' == content.authority
    }

    def "Role 'update' test hits the REST endpoint and parses the JSON output"() {
        when: 'consume the REST URL to update a Role'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.put(new URI('/v1/roles/1'))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content('{"id": 1, "name": "Admin", "authority": "ADMIN"}'))
                    .andReturn().response
            def content = new JsonSlurper().parseText(response.contentAsString)
        then: 'verify the HTTP response'
            1 * roleService.save(_) >> { new Role(id:1,  name:'Admin', authority:'ADMIN') }
            HttpStatus.OK.value() == response.status
            MediaType.APPLICATION_JSON_UTF8_VALUE == response.contentType
            'Admin' == content.name
            'ADMIN' == content.authority
    }
}
