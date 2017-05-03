// ------------------------------------------------------------------------------------------
// Common apiDoc elements
// -- Reusable definitions
// -- Current version of an API definition
// -- Historical versions of API definitions (for comparison, if necessary)
// ------------------------------------------------------------------------------------------

// ------------------------------------------------------------------------------------------
// User List Model
// ------------------------------------------------------------------------------------------
/**
 * @apiDefine UserListModel
 *
 *
 * @apiSuccess {Object[]} users List of users
 * @apiSuccess {String} users.id User ID
 * @apiSuccess {String} users.userName Username of the user
 * @apiSuccess {String} users.email Email
 * @apiSuccess {String} users.firstName First name
 * @apiSuccess {String} users.lastName Last name
 * @apiSuccess {Object[]} users.phones User phone numbers
 * @apiSuccess {String} users.phones.phoneNumber Phone number in E.164 format (ie +16518886021 or +1-651-888-6021 as punctuation is stripped out)
 * @apiSuccess {String} users.phones.phoneType Phone type
 *
 * @apiSuccessExample Success-Response:
 *   HTTP/1.1 200 OK
 *   [
 *       {
 *           "id": "1",
 *           "userName": "admin",
 *           "email": "admin@admin.com",
 *           "firstName": "Admin_First",
 *           "lastName": "Admin_Last",
 *           "phones": [
 *              {"phoneNumber": "+16518886021", "phoneType": "mobile"}
 *           ]
 *       }
 *   ]
 */

// ------------------------------------------------------------------------------------------
// User Request Model
// ------------------------------------------------------------------------------------------
/**
 * @apiDefine UserRequestModel
 *
 * @apiParam {Object} user User
 * @apiParam {String} user.userName Username of the user
 * @apiParam {String} user.email Email
 * @apiParam {String} user.firstName First name
 * @apiParam {String} user.lastName Last name
 * @apiParam {Object[]} user.phones User phone numbers
 * @apiParam {String} user.phones.phoneNumber Phone number
 * @apiParam {String} user.phones.phoneType Phone type (mobile, office, home, other)
 *
 * @apiExample {json} Example body:
 * {
 *     "firstName": "FirstName",
 *     "lastName": "LastName",
 *     "username": "FirstName3@app.com",
 *     "email": "FirstName3@app.com",
 *     "password": "password"
 *     "phones":
 *     [
 *         {
 *             "phoneType": "mobile",
 *             "phoneNumber" : "5555551212",
 *
 *         }
 *     ]
 * }
 */

// ------------------------------------------------------------------------------------------
// User Success Model
// ------------------------------------------------------------------------------------------
/**
 * @apiDefine UserSuccessModel
 *
 * @apiSuccess {Object} user User
 * @apiSuccess {String} user.id User ID
 * @apiSuccess {String} user.userName Username of the user
 * @apiSuccess {String} user.email Email
 * @apiSuccess {String} user.firstName First name
 * @apiSuccess {String} user.lastName Last name
 * @apiSuccess {Object[]} user.phones User phone numbers
 * @apiSuccess {String} user.phones.phoneNumber Phone number
 * @apiSuccess {String} user.phones.phoneType Phone type
 * @apiSuccessExample Success-Response:
 * {
 *     "id": "f9d69894-7908-4606-918e-410dca8c3238",
 *     "firstName": "FirstName",
 *     "lastName": "LastName",
 *     "username": "FirstName3@app.com",
 *     "email": "FirstName3@app.com",
 *     "password": "my-secure-password",
 *     "phones":
 *     [
 *         {
 *             "id": 3,
 *             "userId": "f9d69894-7908-4606-918e-410dca8c3238",
 *             "phoneNumber": "5555551212",
 *             "phoneType": "mobile"
 *         }
 *     ]
 * }
 */

// ------------------------------------------------------------------------------------------
// Username Already In Use Error
// ------------------------------------------------------------------------------------------
/**
 *  @apiDefine UsernameAlreadyInUseError
 *
 *  @apiError UsernameAlreadyInUseException The username is already in use by another user
 *
 *  @apiErrorExample Error-Response
 *  HTTP/1.1 400: Bad Request
 *  {
 *      "error": "400_username_already_in_use",
 *      "errorDescription": "Username already in use by another user. Please choose a different username"
 *  }
 */

// ------------------------------------------------------------------------------------------
// Mobile Phone Number Required Error
// ------------------------------------------------------------------------------------------
/**
 *  @apiDefine MobilePhoneNumberRequiredError
 *
 *  @apiError MobilePhoneNumberRequiredException At least one mobile phone is required
 *
 *  @apiErrorExample Error-Response
 *  HTTP/1.1 400: Bad Request
 *  {
 *      "error": "400_mobile_phone_required",
 *      "errorDescription": "At least one mobile phone is required for a new account"
 *  }
 */

// ------------------------------------------------------------------------------------------
// Too Many Phones Error
// ------------------------------------------------------------------------------------------
/**
 *  @apiDefine TooManyPhonesError
 *
 *  @apiError TooManyPhonesException There should be a limit for user phones
 *
 *  @apiErrorExample Error-Response
 *  HTTP/1.1 400: Bad Request
 *  {
 *      "error": "400_too_many_phones",
 *      "errorDescription": "Too many phones have been added to the profile. Maximum of 5."
 *  }
 */

// ------------------------------------------------------------------------------------------
// Phone Number Parsing Exception
// ------------------------------------------------------------------------------------------
/**
 *  @apiDefine PhoneNumberInvalidError
 *
 *  @apiError PhoneNumberInvalidException Phone number should be in specific format
 *
 *  @apiErrorExample Error-Response
 *  HTTP/1.1 400: Bad Request
 *  {
 *      "error": "400_phone_invalid",
 *      "errorDescription": "The phone number provided is not recognized."
 *  }
 */

