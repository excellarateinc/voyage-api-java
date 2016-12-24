package launchpad.role

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(['/v1/roles', '/v1.0/roles'])
class RoleController {
    private final RoleService roleService

    @Autowired
    RoleController(RoleService roleService) {
        this.roleService = roleService
    }

    /**
     * @api {get} /v1/roles Get all roles
     * @apiVersion 1.0.0
     * @apiName RoleList
     * @apiGroup Role
     *
     * @apiPermission api.roles.list
     *
     * @apiUse AuthHeader
     *
     * @apiSuccess {Object[]} roles List of roles
     * @apiSuccess {String} roles.id Role ID
     * @apiSuccess {String} roles.name name of the role
     * @apiSuccess {String} roles.authority Authority
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 200 OK
     *   [
     *       {
     *           "id": "1",
     *           "name": "Super User",
     *           "authority": "role.super"
     *       }
     *   ]
     *
     * @apiUse UnauthorizedError
     **/
    @GetMapping
    @PreAuthorize("hasAuthority('api.roles.list')")
    ResponseEntity list() {
        Iterable<Role> roles = roleService.listAll()
        return new ResponseEntity(roles, HttpStatus.OK)
    }

    /**
     * @api {post} /v1/roles Create role
     * @apiVersion 1.0.0
     * @apiName RoleCreate
     * @apiGroup Role
     *
     * @apiPermission api.roles.create
     *
     * @apiUse AuthHeader
     *
     * @apiHeader (Response Headers) {String} location Location of the newly created resource
     *
     * @apiHeaderExample {json} Location-Example
     * {
     *     "Location": "http://localhost:52431/api/v1/roles/1"
     * }
     *
     * @apiUse RoleRequestModel
     * @apiUse RoleSuccessModel
     * @apiUse UnauthorizedError
     **/
    @PostMapping
    @PreAuthorize("hasAuthority('api.roles.create')")
    ResponseEntity save(@RequestBody Role role) {
        Role newRole = roleService.save(role)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, "/v1/roles/${newRole.id}")
        return new ResponseEntity(newRole, headers, HttpStatus.CREATED)
    }

    /**
     * @api {get} /v1/roles/:roleId Get a role
     * @apiVersion 1.0.0
     * @apiName RoleGet
     * @apiGroup Role
     *
     * @apiPermission api.roles.get
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} roleId Role ID
     *
     * @apiUse RoleSuccessModel
     * @apiUse UnauthorizedError
     **/
    @GetMapping('/{id}')
    @PreAuthorize("hasAuthority('api.roles.get')")
    ResponseEntity get(@PathVariable('id') long id) {
        Role roleFromDB = roleService.get(id)
        return new ResponseEntity(roleFromDB, HttpStatus.OK)
    }

    /**
     * @api {delete} /v1/roles/:roleId Delete a role
     * @apiVersion 1.0.0
     * @apiName RoleDelete
     * @apiGroup Role
     *
     * @apiPermission api.roles.delete
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} roleId Role ID
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse UnauthorizedError
     * @apiUse BadRequestError
     **/
    @DeleteMapping('/{id}')
    @PreAuthorize("hasAuthority('api.roles.delete')")
    ResponseEntity delete(@PathVariable('id') long id) {
        roleService.delete(id)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {put} /v1/roles/:roleId Update a role
     * @apiVersion 1.0.0
     * @apiName RoleUpdate
     * @apiGroup Role
     *
     * @apiPermission api.roles.update
     *
     * @apiUse AuthHeader
     *
     * @apiUse RoleRequestModel
     * @apiUse RoleSuccessModel
     * @apiUse UnauthorizedError
     **/
    @PutMapping('/{id}')
    @PreAuthorize("hasAuthority('api.roles.update')")
    ResponseEntity update(@RequestBody Role role) {
        Role modifiedRole = roleService.save(role)
        return new ResponseEntity(modifiedRole, HttpStatus.OK)
    }
}
