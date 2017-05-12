// ------------------------------------------------------------------------------------------
// Profile Request Model
// ------------------------------------------------------------------------------------------
/**
 * @apiDefine ProfileRequestModel
 *
 * @apiParam {Object} profile Profile
 * @apiParam {String} profile.userName Username of the user
 * @apiParam {String} profile.email Email (optional)
 * @apiParam {String} profile.firstName First name
 * @apiParam {String} profile.lastName Last name
 * @apiParam {String} profile.password Password
 * @apiParam {Object[]} profile.phones Profile phone numbers
 * @apiParam {String} profile.phones.phoneNumber Phone number in E.164 format (ie +16518886021 or +1-651-888-6021 as punctuation is stripped out)
 * @apiParam {String} profile.phones.phoneType Phone type (MOBILE, OFFICE, HOME, OTHER). NOTE: At least one mobile phone is required.
 *
 * @apiExample {json} Example body:
 * {
 *     "firstName": "FirstName",
 *     "lastName": "LastName",
 *     "username": "FirstName3@app.com",
 *     "email": "FirstName3@app.com",
 *     "password": "password",
 *     "phones":
 *     [
 *         {
 *             "phoneType": "MOBILE",
 *             "phoneNumber" : "+6518886021"
 *         }
 *     ]
 * }
 */

// ------------------------------------------------------------------------------------------
// Profile Success Model
// ------------------------------------------------------------------------------------------
/**
 * @apiDefine ProfileSuccessModel
 *
 * @apiSuccess {Object} profile Profile
 * @apiSuccess {String} profile.id Profile ID
 * @apiSuccess {String} profile.userName Username of the user
 * @apiSuccess {String} profile.email Email
 * @apiSuccess {String} profile.firstName First name
 * @apiSuccess {String} profile.lastName Last name
 * @apiParam {String} profile.password Password
 * @apiSuccess {Object[]} profile.phones Profile phone numbers
 * @apiSuccess {String} profile.phones.phoneNumber Phone number
 * @apiSuccess {String} profile.phones.phoneType Phone type
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
 *             "phoneType": "MOBILE"
 *         }
 *     ]
 * }
 */