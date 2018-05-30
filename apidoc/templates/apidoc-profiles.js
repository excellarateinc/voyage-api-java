/*
 * Copyright 2018 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
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