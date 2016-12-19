package launchpad.role

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
     * @apiPermission role.list
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
     *           "id": "A8DCF6EA-85A9-4D90-B722-3F4B9DE6642A",
     *           "name": "Admin",
     *           "authority": "ROLE_ADMIN"
     *       }
     *   ]
     *
     * @apiUse UnauthorizedError
     **/
    @GetMapping
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
     * @apiPermission lss.permission->create.role
     *
     * @apiUse AuthHeader
     *
     * @apiHeader (Response Headers) {String} location Location of the newly created resource
     *
     * @apiHeaderExample {json} Location-Example
     * {
     *     "Location": "http://localhost:52431/api/v1/roles/b78ae241-1fa6-498c-aa48-9742245d0d2f"
     * }
     *
     * @apiUse RoleRequestModel
     * @apiUse RoleSuccessModel
     * @apiUse UnauthorizedError
     **/
    @PostMapping
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
     * @apiPermission lss.permission->view.role
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} roleId Role ID
     *
     * @apiUse RoleSuccessModel
     * @apiUse UnauthorizedError
     **/
    @GetMapping('/{id}')
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
     * @apiPermission lss.permission->delete.role
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
    ResponseEntity delete(@PathVariable('id') long id) {
        roleService.delete(id)
        return new ResponseEntity(HttpStatus.OK)
    }

    /**
     * @api {put} /v1/roles/:roleId Update a role
     * @apiVersion 1.0.0
     * @apiName RoleUpdate
     * @apiGroup Role
     *
     * @apiPermission role.update
     *
     * @apiUse AuthHeader
     *
     * @apiUse RoleRequestModel
     * @apiUse RoleSuccessModel
     * @apiUse UnauthorizedError
     **/
    @PutMapping('/{id}')
    ResponseEntity update(@RequestBody Role role) {
        Role modifiedRole = roleService.update(role)
        return new ResponseEntity(modifiedRole, HttpStatus.OK)
    }
}
