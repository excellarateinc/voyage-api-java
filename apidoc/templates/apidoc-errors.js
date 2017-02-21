// ------------------------------------------------------------------------------------------
// Common apiDoc elements
// -- Reusable definitions
// -- Current version of an API definition
// -- Historical versions of API definitions (for comparison, if necessary)
// ------------------------------------------------------------------------------------------

// ------------------------------------------------------------------------------------------
// Bad Request Error
// ------------------------------------------------------------------------------------------
/**
 * @apiDefine BadRequestError
 *
 * @apiError BadRequest The input did not pass the model validation.
 *
 * @apiErrorExample Error-Response:
 * HTTP/1.1 400: Bad Request
 * [
 *     {
 *         "code": "user.lastName.required",
 *         "description": "User lastName is a required field"
 *     },
 *     {
 *         "code": "user.email.invalidFormat",
 *         "description": "User email format is invalid. Required format is text@example.com"
 *     }
 * ]
 */

// ------------------------------------------------------------------------------------------
// Not Found Error
// ------------------------------------------------------------------------------------------
/**
 * @apiDefine NotFoundError
 *
 * @apiError NotFound The requested resource was not found
 *
 * @apiErrorExample Error-Response
 * HTTP/1.1 404: Not Found
 * {
 *     "error": "404_unknown_identifier",
 *     "errorDescription": "Unknown record identifier provided"
 * }
 */

// ------------------------------------------------------------------------------------------
// Unauthorized Error
// ------------------------------------------------------------------------------------------
/**
 *  @apiDefine UnauthorizedError
 *
 *  @apiError Unauthorized The user is not authenticated.
 *
 *  @apiErrorExample Error-Response
 *  HTTP/1.1 400: Unauthorized
 *  {
 *      "error": "400_unauthorized",
 *      "errorDescription": "Authorization has been denied for this request."
 *  }
 */
