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
