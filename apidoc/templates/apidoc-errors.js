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

// ------------------------------------------------------------------------------------------
// Record Not Found
// ------------------------------------------------------------------------------------------
/**
 *  @apiDefine UnknownIdentifierError
 *
 *  @apiError UnknownIdentifierException The Object with ID not found in records
 *
 *  @apiErrorExample Error-Response
 *  HTTP/1.1 400: Bad Request
 *  {
 *      "error": "400_unknown_identifier",
 *      "errorDescription": "Unknown record identifier provided"
 *  }
 */

// ------------------------------------------------------------------------------------------
// Record Not Found
// ------------------------------------------------------------------------------------------
/**
 *  @apiDefine ImmutableRecordError
 *
 *  @apiError ImmutableRecordException The requested record is immutable
 *
 *  @apiErrorExample Error-Response
 *  HTTP/1.1 400: Bad Request
 *  {
 *      "error": "400_immutable_record",
 *      "errorDescription": "The requested record is immutable. No changes to this record are allowed."
 *  }
 */

// ------------------------------------------------------------------------------------------
// Invalid Verification Phone Number
// ------------------------------------------------------------------------------------------
/**
 *  @apiDefine InvalidVerificationPhoneNumberError
 *
 *  @apiError InvalidVerificationPhoneNumberException The verification phone number is invalid.
 *
 *  @apiErrorExample Error-Response
 *  HTTP/1.1 400: Bad Request
 *  {
 *      "error": "400_verify_phone_invalid",
 *      "errorDescription": "The verification phone number is invalid."
 *  }
 */

// ------------------------------------------------------------------------------------------
// Code Verification
// ------------------------------------------------------------------------------------------
/**
 *  @apiDefine VerifyCodeExpiredError
 *
 *  @apiError VerifyCodeExpiredException The verification code provided has expired.
 *
 *  @apiErrorExample Error-Response
 *  HTTP/1.1 400: Bad Request
 *  {
 *      "error": "400_verify_code_expired",
 *      "errorDescription": "The verification code provided has expired."
 *  }
 */