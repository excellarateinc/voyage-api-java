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
