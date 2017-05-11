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
