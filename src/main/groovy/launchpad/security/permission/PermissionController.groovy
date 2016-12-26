package launchpad.security.permission

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
@RequestMapping(['/v1/permissions', '/v1.0/permissions'])
class PermissionController {
    private final PermissionService permissionService

    @Autowired
    PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService
    }

    /**
     * @api {get} /v1/permissions Get all permissions
     * @apiVersion 1.0.0
     * @apiName PermissionList
     * @apiGroup Permission
     *
     * @apiPermission api.permissions.list
     *
     * @apiUse AuthHeader
     *
     * @apiSuccess {Object[]} permissions List of permissions
     * @apiSuccess {String} permissions.id Permission ID
     * @apiSuccess {String} permissions.name name of the permission
     * @apiSuccess {String} permissions.description Description
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 200 OK
     *   [
     *       {
     *           "id": "1",
     *           "name": "Super User",
     *           "description": "permission.write"
     *       }
     *   ]
     *
     * @apiUse UnauthorizedError
     **/
    @GetMapping
    @PreAuthorize("hasAuthority('api.permissions.list')")
    ResponseEntity list() {
        Iterable<Permission> permissions = permissionService.listAll()
        return new ResponseEntity(permissions, HttpStatus.OK)
    }

    /**
     * @api {post} /v1/permissions Create permission
     * @apiVersion 1.0.0
     * @apiName PermissionCreate
     * @apiGroup Permission
     *
     * @apiPermission api.permissions.create
     *
     * @apiUse AuthHeader
     *
     * @apiHeader (Response Headers) {String} location Location of the newly created resource
     *
     * @apiHeaderExample {json} Location-Example
     * {
     *     "Location": "http://localhost:52431/api/v1/permissions/1"
     * }
     *
     * @apiUse PermissionRequestModel
     * @apiUse PermissionSuccessModel
     * @apiUse UnauthorizedError
     **/
    @PostMapping
    @PreAuthorize("hasAuthority('api.permissions.create')")
    ResponseEntity save(@RequestBody Permission permission) {
        Permission newPermission = permissionService.save(permission)
        HttpHeaders headers = new HttpHeaders()
        headers.set(HttpHeaders.LOCATION, "/v1/permissions/${newPermission.id}")
        return new ResponseEntity(newPermission, headers, HttpStatus.CREATED)
    }

    /**
     * @api {get} /v1/permissions/:permissionId Get a permission
     * @apiVersion 1.0.0
     * @apiName PermissionGet
     * @apiGroup Permission
     *
     * @apiPermission api.permissions.get
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} permissionId Permission ID
     *
     * @apiUse PermissionSuccessModel
     * @apiUse UnauthorizedError
     **/
    @GetMapping('/{id}')
    @PreAuthorize("hasAuthority('api.permissions.get')")
    ResponseEntity get(@PathVariable('id') long id) {
        Permission permissionFromDB = permissionService.get(id)
        return new ResponseEntity(permissionFromDB, HttpStatus.OK)
    }

    /**
     * @api {delete} /v1/permissions/:permissionId Delete a permission
     * @apiVersion 1.0.0
     * @apiName PermissionDelete
     * @apiGroup Permission
     *
     * @apiPermission api.permissions.delete
     *
     * @apiUse AuthHeader
     *
     * @apiParam {String} permissionId Permission ID
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 204 NO CONTENT
     *
     * @apiUse UnauthorizedError
     * @apiUse BadRequestError
     **/
    @DeleteMapping('/{id}')
    @PreAuthorize("hasAuthority('api.permissions.delete')")
    ResponseEntity delete(@PathVariable('id') long id) {
        permissionService.delete(id)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * @api {put} /v1/permissions/:permissionId Update a permission
     * @apiVersion 1.0.0
     * @apiName PermissionUpdate
     * @apiGroup Permission
     *
     * @apiPermission api.permissions.update
     *
     * @apiUse AuthHeader
     *
     * @apiUse PermissionRequestModel
     * @apiUse PermissionSuccessModel
     * @apiUse UnauthorizedError
     **/
    @PutMapping('/{id}')
    @PreAuthorize("hasAuthority('api.permissions.update')")
    ResponseEntity update(@RequestBody Permission permission) {
        Permission modifiedPermission = permissionService.save(permission)
        return new ResponseEntity(modifiedPermission, HttpStatus.OK)
    }
}
