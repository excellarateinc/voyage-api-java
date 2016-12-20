package launchpad.permission

import groovy.json.JsonSlurper
import launchpad.permission.Permission
import launchpad.permission.PermissionController
import launchpad.permission.PermissionService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

class PermissionControllerIntegrationSpec extends Specification {
    Permission permission
    MockMvc mockMvc
    PermissionService permissionService = Mock(PermissionService)
    PermissionController permissionController = new PermissionController(permissionService)

    def setup() {
        permission = new Permission(id:1, name:'permission.write', description: 'Write permission only')
        mockMvc = MockMvcBuilders.standaloneSetup(permissionController).build()
    }

    def 'Permission list test hits the REST endpoint and parses the JSON output'() {
        when: 'consume the REST URL to retrieve all Permissions'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.get(new URI('/v1/permissions')))
                    .andReturn().response
            def content = new JsonSlurper().parseText(response.contentAsString)
        then: 'verify the HTTP response'
            1 * permissionService.listAll() >> [permission]
            HttpStatus.OK.value() == response.status
            MediaType.APPLICATION_JSON_UTF8_VALUE == response.contentType
            'permission.write' == content[0].name
            'Write permission only' ==  content[0].description
    }

    def "Permission 'get' test hits the REST endpoint and parses the JSON output"() {
        when: 'consume the REST URL to retrieve a specific Permission'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.get(new URI('/v1/permissions/1')))
                    .andReturn().response
            def content = new JsonSlurper().parseText(response.contentAsString)
        then: 'verify the HTTP response'
            1 * permissionService.get(1) >> permission
            HttpStatus.OK.value() == response.status
            MediaType.APPLICATION_JSON_UTF8_VALUE == response.contentType
            'permission.write' == content.name
            'Write permission only' ==  content.description
    }

    def "Permission 'delete' test hits the REST endpoint and parses the JSON output"() {
        when: 'consume the REST URL to delete a specific Permission'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.delete(new URI('/v1/permissions/1')))
                    .andReturn().response
        then: 'verify the HTTP response'
            1 * permissionService.delete(1)
            HttpStatus.OK.value() == response.status
    }

    def "Permission 'create' test hits the REST endpoint and parses the JSON output"() {
        when: 'consume the REST URL to create a new Permission'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.post(new URI('/v1/permissions'))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content('{"name": "permission.write", "description": "ADMIN"}'))
                    .andReturn().response
            def content = new JsonSlurper().parseText(response.contentAsString)
        then: 'verify the HTTP response'
            1 * permissionService.save(_) >> { new Permission(id:1, name:'permission.write', description:'Write permission only') }
            HttpStatus.CREATED.value() == response.status
            MediaType.APPLICATION_JSON_UTF8_VALUE == response.contentType
            '/v1/permissions/1' == response.getHeaderValue(HttpHeaders.LOCATION)
            'permission.write' == content.name
            'Write permission only' == content.description
    }

    def "Permission 'update' test hits the REST endpoint and parses the JSON output"() {
        when: 'consume the REST URL to update a Permission'
            def response = mockMvc
                    .perform(MockMvcRequestBuilders.put(new URI('/v1/permissions/1'))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content('{"id": 1, "name": "permission.read", "description": "Read permission only"}'))
                    .andReturn().response
            def content = new JsonSlurper().parseText(response.contentAsString)
        then: 'verify the HTTP response'
            1 * permissionService.update(_) >> { new Permission(id:1,  name:'permission.read', description:'Read permission only') }
            HttpStatus.OK.value() == response.status
            MediaType.APPLICATION_JSON_UTF8_VALUE == response.contentType
            'permission.read' == content.name
            'Read permission only' == content.description
    }
}
