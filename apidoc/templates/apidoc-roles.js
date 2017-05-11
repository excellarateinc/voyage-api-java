// ------------------------------------------------------------------------------------------
// Common apiDoc elements
// -- Reusable definitions
// -- Current version of an API definition
// -- Historical versions of API definitions (for comparison, if necessary)
// ------------------------------------------------------------------------------------------

// ------------------------------------------------------------------------------------------
// Roles List Model
// ------------------------------------------------------------------------------------------
/**
 * @apiDefine RoleListModel
 *
 * @apiSuccess {Object[]} roles List of roles
 * @apiSuccess {String} roles.id Role ID
 * @apiSuccess {String} roles.name name of the role
 * @apiSuccess {String} roles.authority Description
 *
 * @apiSuccessExample Success-Response:
 * {
 *   HTTP/1.1 200 OK
 *   [
 *       {
 *           "id": "f9d69894-7908-4606-918e-410dca8c3238",
 *           "name": "Super User",
 *           "authority": "roles.super_user"
 *       }
 *   ]
 * }
 */
// ------------------------------------------------------------------------------------------
// Role Request Model
// ------------------------------------------------------------------------------------------
/**
 * @apiDefine RoleRequestModel
 *
 * @apiParam {Object} role Role
 * @apiParam {String} role.name Role name
 * @apiParam {String} role.authority Role description
 *
 * @apiExample {json} Example body:
 * {
 *     "name": "Super User",
 *     "authority": "roles.super_user"
 * }
 */

// ------------------------------------------------------------------------------------------
// Role Success Model
// ------------------------------------------------------------------------------------------
/**
 * @apiDefine RoleSuccessModel
 *
 * @apiSuccess {Object} role Role
 * @apiSuccess {String} roles.id Role ID
 * @apiSuccess {String} roles.name name of the role
 * @apiSuccess {String} roles.authority Description
 *
 * @apiSuccessExample Success-Response:
 * {
 *     "id": "f9d69894-7908-4606-918e-410dca8c3238",
 *     "name": "Super User",
 *     "authority": "roles.super_user"
 * }
 */
