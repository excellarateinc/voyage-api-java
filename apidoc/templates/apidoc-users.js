/*
 * Copyright 2017 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ------------------------------------------------------------------------------------------
// Common apiDoc elements
// -- Reusable definitions
// -- Current version of an API definition
// -- Historical versions of API definitions (for comparison, if necessary)
// ------------------------------------------------------------------------------------------

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
 *     "phones":
 *     [
 *         {
 *             "phoneType": "mobile",
 *             "phoneNumber" : "5555551212"
 *         }
 *     ],
 *     "isEnabled": true
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
 *     "phones":
 *     [
 *         {
 *             "id": 3,
 *             "userId": "f9d69894-7908-4606-918e-410dca8c3238",
 *             "phoneNumber": "5555551212",
 *             "phoneType": "mobile"
 *         }
 *     ],
 *     "isActive": true
 * }
 */

// ------------------------------------------------------------------------------------------
// Username Already In Use Error
// ------------------------------------------------------------------------------------------
/**
 *  @apiDefine UsernameAlreadyInUseError
 *
 *  @apiError BadRequest The username is already in use by another user
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
 *  @apiError BadRequest At least one mobile phone is required
 *
 *  @apiErrorExample Error-Response
 *  HTTP/1.1 400: Bad Request
 *  {
 *      "error": "400_mobile_phone_required",
 *      "errorDescription": "At least one mobile phone is required for a new account"
 *  }
 */