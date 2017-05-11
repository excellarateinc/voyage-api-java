// ------------------------------------------------------------------------------------------
// Common apiDoc elements
// -- Reusable definitions
// -- Current version of an API definition
// -- Historical versions of API definitions (for comparison, if necessary)
// ------------------------------------------------------------------------------------------

// ------------------------------------------------------------------------------------------
// Permission List Model
// ------------------------------------------------------------------------------------------
/**
 * @apiDefine PermissionListModel
 *
 * @apiSuccess {Object[]} permissions List of permissions
 * @apiSuccess {String} permissions.id Permission ID
 * @apiSuccess {String} permissions.name name of the permission
 * @apiSuccess {String} permissions.description Description
 *
 * @apiSuccessExample Success-Response:
 * {
 *   HTTP/1.1 200 OK
 *   [
 *       {
 *           "id": "f9d69894-7908-4606-918e-410dca8c3238",
 *           "name": "api.users.list",
 *           "description": "users GET web service endpoint to return a full list of users"
 *       }
 *   ]
 * }
 */

// ------------------------------------------------------------------------------------------
// Permission Request Model
// ------------------------------------------------------------------------------------------
/**
 * @apiDefine PermissionRequestModel
 *
 * @apiParam {Object} permission Permission
 * @apiParam {String} permission.name Permission name
 * @apiParam {String} permission.description Permission description
 *
 * @apiExample {json} Example body:
 * {
 *           "name": "api.users.list",
 *           "description": "users GET web service endpoint to return a full list of users"
 * }
 */

// ------------------------------------------------------------------------------------------
// Permission Success Model
// ------------------------------------------------------------------------------------------
/**
 * @apiDefine PermissionSuccessModel
 *
 * @apiSuccess {Object} permission Permission
 * @apiSuccess {String} permission.id Permission ID
 * @apiSuccess {String} permission.name name of the permission
 * @apiSuccess {String} permission.description Description
 *
 * @apiSuccessExample Success-Response:
 * {
 *     "id": "f9d69894-7908-4606-918e-410dca8c3238",
 *      "name": "api.users.list",
 *      "description": "users GET web service endpoint to return a full list of users"
 * }
 */
